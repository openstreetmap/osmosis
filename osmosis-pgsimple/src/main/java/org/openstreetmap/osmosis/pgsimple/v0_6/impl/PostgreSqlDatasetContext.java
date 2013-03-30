// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_6.DatasetContext;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainerIterator;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainerIterator;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.PolygonBuilder;
import org.openstreetmap.osmosis.pgsimple.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsimple.v0_6.PostgreSqlVersionConstants;
import org.openstreetmap.osmosis.core.store.MultipleSourceIterator;
import org.openstreetmap.osmosis.core.store.ReleasableAdaptorForIterator;
import org.openstreetmap.osmosis.core.store.UpcastIterator;


/**
 * Provides read-only access to a PostgreSQL dataset store. Each thread
 * accessing the store must create its own reader. It is important that all
 * iterators obtained from this reader are released before releasing the reader
 * itself.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetContext implements DatasetContext {
	
	private static final Logger LOG = Logger.getLogger(PostgreSqlDatasetContext.class.getName());
	
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private DatabaseCapabilityChecker capabilityChecker;
	private boolean initialized;
	private DatabaseContext dbCtx;
	private UserDao userDao;
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	private PostgreSqlEntityManager<Node> nodeManager;
	private PostgreSqlEntityManager<Way> wayManager;
	private PostgreSqlEntityManager<Relation> relationManager;
	private PolygonBuilder polygonBuilder;
	private ReleasableContainer releasableContainer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlDatasetContext(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		
		polygonBuilder = new PolygonBuilder();
		
		releasableContainer = new ReleasableContainer();
		
		initialized = false;
	}
	
	
	/**
	 * Initialises the database connection and associated data access objects.
	 */
	private void initialize() {
		if (dbCtx == null) {
			ActionDao actionDao;
			
			dbCtx = new DatabaseContext(loginCredentials);
			
			new SchemaVersionValidator(dbCtx, preferences).validateVersion(
					PostgreSqlVersionConstants.SCHEMA_VERSION);
			
			capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
			
			actionDao = releasableContainer.add(new ActionDao(dbCtx));
			userDao = releasableContainer.add(new UserDao(dbCtx, actionDao));
			nodeDao = releasableContainer.add(new NodeDao(dbCtx, actionDao));
			wayDao = releasableContainer.add(new WayDao(dbCtx, actionDao));
			relationDao = releasableContainer.add(new RelationDao(dbCtx, actionDao));
			
			nodeManager = new PostgreSqlEntityManager<Node>(nodeDao, userDao);
			wayManager = new PostgreSqlEntityManager<Way>(wayDao, userDao);
			relationManager = new PostgreSqlEntityManager<Relation>(relationDao, userDao);
		}
		
		initialized = true;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Node getNode(long id) {
		return getNodeManager().getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Way getWay(long id) {
		return getWayManager().getEntity(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Relation getRelation(long id) {
		return getRelationManager().getEntity(id);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Node> getNodeManager() {
		if (!initialized) {
			initialize();
		}
		
		return nodeManager;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Way> getWayManager() {
		if (!initialized) {
			initialize();
		}
		
		return wayManager;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityManager<Relation> getRelationManager() {
		if (!initialized) {
			initialize();
		}
		
		return relationManager;
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
		
		sources.add(new UpcastIterator<EntityContainer, BoundContainer>(
				new BoundContainerIterator(new ReleasableAdaptorForIterator<Bound>(bounds.iterator()))));
		sources.add(new UpcastIterator<EntityContainer, NodeContainer>(
				new NodeContainerIterator(nodeDao.iterate())));
		sources.add(new UpcastIterator<EntityContainer, WayContainer>(
				new WayContainerIterator(wayDao.iterate())));
		sources.add(new UpcastIterator<EntityContainer, RelationContainer>(
				new RelationContainerIterator(relationDao.iterate())));
		
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
			// PostgreSQL sometimes incorrectly chooses to perform full table scans, these options
			// prevent this. Note that this is not recommended practice according to documentation
			// but fixing this would require modifying the table statistics gathering
			// configuration to produce better plans.
			dbCtx.executeStatement("SET enable_seqscan = false");
			dbCtx.executeStatement("SET enable_mergejoin = false");
			dbCtx.executeStatement("SET enable_hashjoin = false");
			
			// Create a temporary table capable of holding node ids.
			LOG.finer("Creating node id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_node_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding way ids.
			LOG.finer("Creating way id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_way_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			// Create a temporary table capable of holding relation ids.
			LOG.finer("Creating relation id temp table.");
			dbCtx.executeStatement("CREATE TEMPORARY TABLE box_relation_list (id bigint PRIMARY KEY) ON COMMIT DROP");
			
			// Build a polygon representing the bounding box.
			// Sample box for query testing may be:
			// GeomFromText('POLYGON((144.93912192855174 -37.82981987499741,
			// 144.93912192855174 -37.79310006709244, 144.98188026000003
			// -37.79310006709244, 144.98188026000003 -37.82981987499741,
			// 144.93912192855174 -37.82981987499741))', -1)
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
			LOG.finer("Selecting all node ids inside bounding box.");
			preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_node_list SELECT id FROM nodes WHERE (geom && ?)");
			prmIndex = 1;
			preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			LOG.finer(rowCount + " rows affected.");
			
			// Select all ways inside the bounding box into the way temp table.
			if (capabilityChecker.isWayLinestringSupported()) {
				LOG.finer("Selecting all way ids inside bounding box using way linestring geometry.");
				// We have full way geometry available so select ways
				// overlapping the requested bounding box.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list "
						+ "SELECT id FROM ways w where w.linestring && ?"
				);
				prmIndex = 1;
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				
			} else if (capabilityChecker.isWayBboxSupported()) {
				LOG.finer("Selecting all way ids inside bounding box using dynamically built"
						+ " way linestring with way bbox indexing.");
				// The inner query selects the way id and node coordinates for
				// all ways constrained by the way bounding box which is
				// indexed.
				// The middle query converts the way node coordinates into
				// linestrings.
				// The outer query constrains the query to the linestrings
				// inside the bounding box. These aren't indexed but the inner
				// query way bbox constraint will minimise the unnecessary data.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list "
						+ "SELECT way_id FROM ("
						+ "SELECT c.way_id AS way_id, ST_MakeLine(c.geom) AS way_line FROM ("
						+ "SELECT w.id AS way_id, n.geom AS geom FROM nodes n"
						+ " INNER JOIN way_nodes wn ON n.id = wn.node_id"
						+ " INNER JOIN ways w ON wn.way_id = w.id"
						+ " WHERE (w.bbox && ?) ORDER BY wn.way_id, wn.sequence_id"
						+ ") c "
						+ "GROUP BY c.way_id"
						+ ") w "
						+ "WHERE (w.way_line && ?)"
				);
				prmIndex = 1;
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				preparedStatement.setObject(prmIndex++, new PGgeometry(bboxPolygon));
				
			} else {
				LOG.finer("Selecting all way ids inside bounding box using already selected nodes.");
				// No way bbox support is available so select ways containing
				// the selected nodes.
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_way_list "
						+ "SELECT wn.way_id FROM way_nodes wn INNER JOIN box_node_list n ON wn.node_id = n.id"
						+ " GROUP BY wn.way_id"
				);
			}
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			LOG.finer(rowCount + " rows affected.");
			
			// Select all relations containing the nodes or ways into the relation table.
			LOG.finer("Selecting all relation ids containing selected nodes or ways.");
			preparedStatement = dbCtx.prepareStatement(
				"INSERT INTO box_relation_list ("
					+ "SELECT rm.relation_id AS relation_id FROM relation_members rm"
					+ " INNER JOIN box_node_list n ON rm.member_id = n.id WHERE rm.member_type = ? "
					+ "UNION "
					+ "SELECT rm.relation_id AS relation_id FROM relation_members rm"
					+ " INNER JOIN box_way_list w ON rm.member_id = w.id WHERE rm.member_type = ?"
					+ ")"
			);
			prmIndex = 1;
			preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Node));
			preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Way));
			rowCount = preparedStatement.executeUpdate();
			preparedStatement.close();
			preparedStatement = null;
			LOG.finer(rowCount + " rows affected.");
			
			// Include all relations containing the current relations into the
			// relation table and repeat until no more inclusions occur.
			do {
				LOG.finer("Selecting parent relations of selected relations.");
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_relation_list "
						+ "SELECT rm.relation_id AS relation_id FROM relation_members rm"
						+ " INNER JOIN box_relation_list r ON rm.member_id = r.id WHERE rm.member_type = ? "
						+ "EXCEPT "
						+ "SELECT id AS relation_id FROM box_relation_list"
				);
				prmIndex = 1;
				preparedStatement.setString(prmIndex++, memberTypeValueMapper.getMemberType(EntityType.Relation));
				rowCount = preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
				LOG.finer(rowCount + " rows affected.");
			} while (rowCount > 0);
			
			// If complete ways is set, select all nodes contained by the ways into the node temp table.
			if (completeWays) {
				LOG.finer("Selecting all node ids for selected ways.");
				preparedStatement = dbCtx.prepareStatement(
					"INSERT INTO box_node_list "
						+ "SELECT wn.node_id AS id FROM way_nodes wn INNER JOIN box_way_list bw ON wn.way_id = bw.id "
						+ "EXCEPT "
						+ "SELECT id AS node_id FROM box_node_list"
				);
				prmIndex = 1;
				rowCount = preparedStatement.executeUpdate();
				preparedStatement.close();
				preparedStatement = null;
				LOG.finer(rowCount + " rows affected.");
			}
			
			// Analyse the temporary tables to give the query planner the best chance of producing good queries.
			dbCtx.executeStatement("ANALYZE box_node_list");
			dbCtx.executeStatement("ANALYZE box_way_list");
			dbCtx.executeStatement("ANALYZE box_relation_list");
			
			// Create iterators for the selected records for each of the entity types.
			LOG.finer("Iterating over results.");
			resultSets = new ArrayList<ReleasableIterator<EntityContainer>>();
			resultSets.add(
					new UpcastIterator<EntityContainer, BoundContainer>(
							new BoundContainerIterator(new ReleasableAdaptorForIterator<Bound>(bounds.iterator()))));
			resultSets.add(
					new UpcastIterator<EntityContainer, NodeContainer>(
							new NodeContainerIterator(new NodeReader(dbCtx, "box_node_list"))));
			resultSets.add(
					new UpcastIterator<EntityContainer, WayContainer>(
							new WayContainerIterator(new WayReader(dbCtx, "box_way_list"))));
			resultSets.add(
					new UpcastIterator<EntityContainer, RelationContainer>(
							new RelationContainerIterator(new RelationReader(dbCtx, "box_relation_list"))));
			
			// Merge all readers into a single result iterator and return.			
			return new MultipleSourceIterator<EntityContainer>(resultSets);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to perform bounding box queries.", e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close prepared statement.", e);
				}
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		if (dbCtx != null) {
			dbCtx.commit();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		releasableContainer.release();
		releasableContainer.clear();
		
		if (dbCtx != null) {
			dbCtx.release();
			
			dbCtx = null;
		}
	}
}
