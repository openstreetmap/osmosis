// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.DatasetReader;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.NodeContainerIterator;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainerIterator;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainerIterator;
import com.bretth.osmosis.core.customdb.v0_5.impl.MultipleSourceIterator;
import com.bretth.osmosis.core.customdb.v0_5.impl.UpcastIterator;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.pdb.v0_5.PostgreSqlVersionConstants;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Provides read-only access to a PostgreSQL dataset store. Each thread
 * accessing the store must create its own reader. It is important that all
 * iterators obtained from this reader are released before releasing the reader
 * itself.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetReader implements DatasetReader {
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private boolean initialized;
	private DatabaseContext dbCtx;
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlDatasetReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		
		initialized = false;
	}
	
	
	/**
	 * Initialises the database connection and associated data access objects.
	 */
	private void initialize() {
		if (dbCtx == null) {
			dbCtx = new DatabaseContext(loginCredentials);
			
			if (preferences.getValidateSchemaVersion()) {
				new SchemaVersionValidator(loginCredentials).validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			}
			
			nodeDao = new NodeDao(dbCtx);
			wayDao = new WayDao(dbCtx);
			relationDao = new RelationDao(dbCtx);
		}
		
		initialized = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getNode(long id) {
		if (!initialized) {
			initialize();
		}
		
		return nodeDao.getNode(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		if (!initialized) {
			initialize();
		}
		
		return wayDao.getWay(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		if (!initialized) {
			initialize();
		}
		
		return relationDao.getRelation(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterate() {
		List<ReleasableIterator<EntityContainer>> sources;
		
		if (!initialized) {
			initialize();
		}
		
		sources = new ArrayList<ReleasableIterator<EntityContainer>>();
		
		sources.add(new UpcastIterator<EntityContainer, NodeContainer>(new NodeContainerIterator(nodeDao.iterate())));
		sources.add(new UpcastIterator<EntityContainer, WayContainer>(new WayContainerIterator(wayDao.iterate())));
		sources.add(new UpcastIterator<EntityContainer, RelationContainer>(new RelationContainerIterator(relationDao.iterate())));
		
		return new MultipleSourceIterator<EntityContainer>(sources);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterateBoundingBox(
			double left, double right, double top, double bottom, boolean completeWays) {
		PreparedStatement preparedStatement = null;
		int prmIndex;
		Point[] bboxPoints;
		Polygon bboxPolygon;
		MemberTypeValueMapper memberTypeValueMapper;
		int rowCount;
		List<ReleasableIterator<EntityContainer>> resultSets;
		
		try {
			// Create a temporary table capable of holding node ids.
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_node_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding way ids.
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_way_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding relation ids.
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_relation_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			
			// Build a polygon representing the bounding box.
			bboxPoints = new Point[4];
			bboxPoints[0] = new Point(left, bottom);
			bboxPoints[1] = new Point(left, top);
			bboxPoints[2] = new Point(right, top);
			bboxPoints[3] = new Point(right, bottom);
			bboxPolygon = new Polygon(new LinearRing[] {new LinearRing(bboxPoints)});
			
			// Instantiate the mapper for converting between entity types and
			// member type values.
			memberTypeValueMapper = new MemberTypeValueMapper();
			
			// Select all nodes inside the box into the node temp table.
			preparedStatement = dbCtx.prepareStatement("INSERT INTO box_node_list SELECT id FROM node WHERE (coordinate && ?)");
			prmIndex = 1;
			preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
			preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			
			// Select all ways inside the bounding box into the way temp table.
			// The inner query selects the way id and node coordinates for all ways constrained by the way bounding box which is indexed.
			// The middle query converts the way node coordinates into linestrings.
			// The outer query constrains the query to the linestrings inside the bounding box.  These aren't indexed but the inner query
			// way bbox constraint will minimise the unnecessary data.
			preparedStatement = dbCtx.prepareStatement(
				"SELECT way_id FROM (" +
				"SELECT c.way_id AS way_id, MakeLine(c.coordinate) AS way_line FROM (" +
				"SELECT w.id AS way_id, n.coordinate AS coordinate FROM node n INNER JOIN way_node wn ON n.id = wn.node_id INNER JOIN way w ON wn.way_id = w.id WHERE w.bbox && ? ORDER BY wn.way_id, wn.sequence_id" +
				") c" +
				"GROUP BY c.way_id" +
				") w" +
				"WHERE w.way_line && ?"
			);
			prmIndex = 1;
			preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
			preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
			preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			
			// Select all relations containing the nodes or ways into the relation table.
			preparedStatement = dbCtx.prepareStatement(
				"INSERT INTO box_relation_list (" +
				"SELECT rm.relation_id AS relation_id FROM relation_member rm INNER JOIN box_node_list n ON rm.member_id = n.id WHERE rm.member_type = ?" +
				"UNION" +
				"SELECT rm.relation_id AS relation_id FROM relation_member rm INNER JOIN box_way_list w ON rm.member_id = w.id WHERE rm.member_type = ?" +
				")"
			);
			prmIndex = 1;
			preparedStatement.setInt(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Node));
			preparedStatement.setInt(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Way));
			preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			
			// Include all relations containing the current relations into the
			// relation table and repeat until no more inclusions occur.
			do {
				preparedStatement = dbCtx.prepareStatement(
					"SELECT rm.relation_id AS relation_id FROM relation_member rm INNER JOIN box_relation_list r ON rm.member_id = r.id WHERE rm.member_type = ?" +
					"EXCEPT" +
					"SELECT id AS relation_id FROM box_relation_list"
				);
				prmIndex = 1;
				preparedStatement.setInt(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Relation));
				rowCount = preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
			} while (rowCount > 0);
			
			// If complete ways is set, select all nodes contained by the ways into the node temp table.
			if (completeWays) {
				preparedStatement = dbCtx.prepareStatement(
					"SELECT n.id AS node_id FROM node n INNER JOIN way_node wn ON n.id = wn.node_id INNER JOIN box_way_list bw ON wn.way_id = bw.id" +
					"EXCEPT" +
					"SELECT id AS node_id FROM box_node_list"
				);
				prmIndex = 1;
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
			}
			
			// Create iterators for the selected records for each of the entity types.
			resultSets = new ArrayList<ReleasableIterator<EntityContainer>>(3);
			resultSets.add(new UpcastIterator<EntityContainer, NodeContainer>(new NodeContainerIterator(new NodeReader(dbCtx, "box_node_list"))));
			resultSets.add(new UpcastIterator<EntityContainer, WayContainer>(new WayContainerIterator(new WayReader(dbCtx, "box_way_list"))));
			resultSets.add(new UpcastIterator<EntityContainer, RelationContainer>(new RelationContainerIterator(new RelationReader(dbCtx, "box_relation_list"))));
			
			// Merge all readers into a single result iterator and return.			
			return new MultipleSourceIterator<EntityContainer>(resultSets);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to perform bounding box queries.", e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (nodeDao != null) {
			nodeDao.release();
		}
		if (wayDao != null) {
			wayDao.release();
		}
		if (relationDao != null) {
			relationDao.release();
		}
		if (dbCtx != null) {
			dbCtx.release();
			
			dbCtx = null;
		}
	}
}
