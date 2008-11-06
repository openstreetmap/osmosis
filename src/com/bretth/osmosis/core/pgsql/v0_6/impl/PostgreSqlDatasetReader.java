// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import com.bretth.osmosis.core.OsmosisConstants;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.BoundContainerIterator;
import com.bretth.osmosis.core.container.v0_6.DatasetReader;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainerIterator;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainerIterator;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainerIterator;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.PolygonBuilder;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlVersionConstants;
import com.bretth.osmosis.core.store.IteratorReleasableIterator;
import com.bretth.osmosis.core.store.MultipleSourceIterator;
import com.bretth.osmosis.core.store.UpcastIterator;


/**
 * Provides read-only access to a PostgreSQL dataset store. Each thread
 * accessing the store must create its own reader. It is important that all
 * iterators obtained from this reader are released before releasing the reader
 * itself.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetReader implements DatasetReader {
	
	private static final Logger log = Logger.getLogger(PostgreSqlDatasetReader.class.getName());
	
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private DatabaseCapabilityChecker capabilityChecker;
	private boolean initialized;
	private DatabaseContext dbCtx;
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	private PolygonBuilder polygonBuilder;
	
	
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
		
		polygonBuilder = new PolygonBuilder();
		
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
			
			capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
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
		
		return nodeDao.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWay(long id) {
		if (!initialized) {
			initialize();
		}
		
		return wayDao.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getRelation(long id) {
		if (!initialized) {
			initialize();
		}
		
		return relationDao.getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<EntityContainer> iterate() {
		List<Bound> bounds;
		List<ReleasableIterator<EntityContainer>> sources;
		
		if (!initialized) {
			initialize();
		}
		
		// Build the bounds list.
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound("Osmosis " + OsmosisConstants.VERSION));
		
		sources = new ArrayList<ReleasableIterator<EntityContainer>>();
		
		sources.add(new UpcastIterator<EntityContainer, BoundContainer>(new BoundContainerIterator(new IteratorReleasableIterator<Bound>(bounds.iterator()))));
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
		List<Bound> bounds;
		PreparedStatement preparedStatement = null;
		int prmIndex;
		Point[] bboxPoints;
		Polygon bboxPolygon;
		MemberTypeValueMapper memberTypeValueMapper;
		int rowCount;
		List<ReleasableIterator<EntityContainer>> resultSets;
		
		if (!initialized) {
			initialize();
		}
		
		// Build the bounds list.
		bounds = new ArrayList<Bound>();
		bounds.add(new Bound(right, left, top, bottom, "Osmosis " + OsmosisConstants.VERSION));
		
		try {
			dbCtx.executeStatement("SET enable_hashjoin = false");
			// Create a temporary table capable of holding node ids.
			log.finer("Creating node id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_node_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding way ids.
			log.finer("Creating way id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_way_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding relation ids.
			log.finer("Creating relation id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_relation_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			
			// Build a polygon representing the bounding box.
			// Sample box for query testing may be:
			// GeomFromText('POLYGON((144.93912192855174 -37.82981987499741, 144.93912192855174 -37.79310006709244, 144.98188026000003 -37.79310006709244, 144.98188026000003 -37.82981987499741, 144.93912192855174 -37.82981987499741))', -1)
			bboxPoints = new Point[5];
			bboxPoints[0] = new Point(left, bottom);
			bboxPoints[1] = new Point(left, top);
			bboxPoints[2] = new Point(right, top);
			bboxPoints[3] = new Point(right, bottom);
			bboxPoints[4] = new Point(left, bottom);
			bboxPolygon = polygonBuilder.createPolygon(bboxPoints);
			
			// Instantiate the mapper for converting between entity types and
			// member type values.
			memberTypeValueMapper = new MemberTypeValueMapper();
			
			// Select all nodes inside the box into the node temp table.
			log.finer("Selecting all node ids inside bounding box.");
			preparedStatement = dbCtx.prepareStatement("INSERT INTO box_node_list SELECT id FROM nodes WHERE (geom && ?)");
			prmIndex = 1;
			preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			log.finer(rowCount + " rows affected.");
			
			// Select all ways inside the bounding box into the way temp table.
			if (capabilityChecker.isWayLinestringSupported()) {
				log.finer("Selecting all way ids inside bounding box using way linestring geometry.");
				// We have full way geometry available so select ways
				// overlapping the requested bounding box.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list " +
					"SELECT id FROM ways w where w.linestring && ?"
				);
				prmIndex = 1;
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				
			} else if (capabilityChecker.isWayBboxSupported()) {
				log.finer("Selecting all way ids inside bounding box using dynamically built way linestring with way bbox indexing.");
				// The inner query selects the way id and node coordinates for
				// all ways constrained by the way bounding box which is
				// indexed.
				// The middle query converts the way node coordinates into
				// linestrings.
				// The outer query constrains the query to the linestrings
				// inside the bounding box. These aren't indexed but the inner
				// query way bbox constraint will minimise the unnecessary data.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list " +
					"SELECT way_id FROM (" +
					"SELECT c.way_id AS way_id, MakeLine(c.geom) AS way_line FROM (" +
					"SELECT w.id AS way_id, n.geom AS geom FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id INNER JOIN ways w ON wn.way_id = w.id WHERE (w.bbox && ?) ORDER BY wn.way_id, wn.sequence_id" +
					") c " +
					"GROUP BY c.way_id" +
					") w " +
					"WHERE (w.way_line && ?)"
				);
				prmIndex = 1;
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				
			} else {
				log.finer("Selecting all way ids inside bounding box using already selected nodes.");
				// No way bbox support is available so select ways containing
				// the selected nodes.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list " +
					"SELECT wn.way_id FROM way_nodes wn INNER JOIN box_node_list n ON wn.node_id = n.id GROUP BY wn.way_id"
				);
			}
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			log.finer(rowCount + " rows affected.");
			
			// Select all relations containing the nodes or ways into the relation table.
			log.finer("Selecting all relation ids containing selected nodes or ways.");
			preparedStatement = dbCtx.prepareStatement(
				"INSERT INTO box_relation_list (" +
				"SELECT rm.relation_id AS relation_id FROM relation_members rm INNER JOIN box_node_list n ON rm.member_id = n.id WHERE rm.member_type = ? " +
				"UNION " +
				"SELECT rm.relation_id AS relation_id FROM relation_members rm INNER JOIN box_way_list w ON rm.member_id = w.id WHERE rm.member_type = ?" +
				")"
			);
			prmIndex = 1;
			preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Node));
			preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Way));
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			log.finer(rowCount + " rows affected.");
			
			// Include all relations containing the current relations into the
			// relation table and repeat until no more inclusions occur.
			do {
				log.finer("Selecting parent relations of selected relations.");
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_relation_list " +
					"SELECT rm.relation_id AS relation_id FROM relation_members rm INNER JOIN box_relation_list r ON rm.member_id = r.id WHERE rm.member_type = ? " +
					"EXCEPT " +
					"SELECT id AS relation_id FROM box_relation_list"
				);
				prmIndex = 1;
				preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Relation));
				rowCount = preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
				log.finer(rowCount + " rows affected.");
			} while (rowCount > 0);
			
			// If complete ways is set, select all nodes contained by the ways into the node temp table.
			if (completeWays) {
				log.finer("Selecting all node ids for selected ways.");
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_node_list " +
					"SELECT wn.node_id AS id FROM way_nodes wn INNER JOIN box_way_list bw ON wn.way_id = bw.id " +
					"EXCEPT " +
					"SELECT id AS node_id FROM box_node_list"
				);
				prmIndex = 1;
				rowCount = preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
				log.finer(rowCount + " rows affected.");
			}
			
			// Create iterators for the selected records for each of the entity types.
			log.finer("Iterating over results.");
			resultSets = new ArrayList<ReleasableIterator<EntityContainer>>();
			resultSets.add(new UpcastIterator<EntityContainer, BoundContainer>(new BoundContainerIterator(new IteratorReleasableIterator<Bound>(bounds.iterator()))));
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
