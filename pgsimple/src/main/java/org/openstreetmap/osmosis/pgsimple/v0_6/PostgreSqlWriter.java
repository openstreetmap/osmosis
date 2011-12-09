// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.database.ReleasableStatementContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.pgsimple.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.ActionDao;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.IndexManager;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.NodeMapper;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.RelationMapper;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.RelationMemberMapper;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.TagMapper;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.UserDao;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.WayGeometryBuilder;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.WayMapper;
import org.openstreetmap.osmosis.pgsimple.v0_6.impl.WayNodeMapper;
import org.postgis.Geometry;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriter implements Sink, EntityProcessor {
	
	private static final Logger LOG = Logger.getLogger(PostgreSqlWriter.class.getName());
	
	// These constants define how many rows of each data type will be inserted
	// with single insert statements.
	private static final int INSERT_BULK_ROW_COUNT_NODE = 1000;
	private static final int INSERT_BULK_ROW_COUNT_NODE_TAG = 1000;
	private static final int INSERT_BULK_ROW_COUNT_WAY = 1000;
	private static final int INSERT_BULK_ROW_COUNT_WAY_TAG = 1000;
	private static final int INSERT_BULK_ROW_COUNT_WAY_NODE = 1000;
	private static final int INSERT_BULK_ROW_COUNT_RELATION = 1000;
	private static final int INSERT_BULK_ROW_COUNT_RELATION_TAG = 1000;
	private static final int INSERT_BULK_ROW_COUNT_RELATION_MEMBER = 1000;
	
	
	private DatabaseContext dbCtx;
	private boolean enableBboxBuilder;
	private boolean enableLinestringBuilder;
	private SchemaVersionValidator schemaVersionValidator;
	private IndexManager indexManager;
	private List<Node> nodeBuffer;
	private List<DbFeature<Tag>> nodeTagBuffer;
	private List<Way> wayBuffer;
	private List<DbFeature<Tag>> wayTagBuffer;
	private List<DbOrderedFeature<WayNode>> wayNodeBuffer;
	private List<Relation> relationBuffer;
	private List<DbFeature<Tag>> relationTagBuffer;
	private List<DbOrderedFeature<RelationMember>> relationMemberBuffer;
	private boolean initialized;
	private HashSet<Integer> userSet;
	private ActionDao actionDao;
	private UserDao userDao;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement singleNodeStatement;
	private PreparedStatement bulkNodeStatement;
	private PreparedStatement singleNodeTagStatement;
	private PreparedStatement bulkNodeTagStatement;
	private PreparedStatement singleWayStatement;
	private PreparedStatement bulkWayStatement;
	private PreparedStatement singleWayTagStatement;
	private PreparedStatement bulkWayTagStatement;
	private PreparedStatement singleWayNodeStatement;
	private PreparedStatement bulkWayNodeStatement;
	private PreparedStatement singleRelationStatement;
	private PreparedStatement bulkRelationStatement;
	private PreparedStatement singleRelationTagStatement;
	private PreparedStatement bulkRelationTagStatement;
	private PreparedStatement singleRelationMemberStatement;
	private PreparedStatement bulkRelationMemberStatement;
	private int uncommittedEntityCount;
	private NodeMapper nodeBuilder;
	private WayMapper wayBuilder;
	private RelationMapper relationBuilder;
	private TagMapper nodeTagBuilder;
	private TagMapper wayTagBuilder;
	private TagMapper relationTagBuilder;
	private WayNodeMapper wayNodeBuilder;
	private RelationMemberMapper relationMemberBuilder;
	private WayGeometryBuilder wayGeometryBuilder;


	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public PostgreSqlWriter(
			DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			boolean enableBboxBuilder, boolean enableLinestringBuilder, NodeLocationStoreType storeType) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		this.enableBboxBuilder = enableBboxBuilder;
		this.enableLinestringBuilder = enableLinestringBuilder;
		
		schemaVersionValidator = new SchemaVersionValidator(dbCtx, preferences);
		indexManager = new IndexManager(dbCtx, !enableBboxBuilder, !enableLinestringBuilder);
		
		nodeBuffer = new ArrayList<Node>();
		nodeTagBuffer = new ArrayList<DbFeature<Tag>>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<DbFeature<Tag>>();
		wayNodeBuffer = new ArrayList<DbOrderedFeature<WayNode>>();
		relationBuffer = new ArrayList<Relation>();
		relationTagBuffer = new ArrayList<DbFeature<Tag>>();
		relationMemberBuffer = new ArrayList<DbOrderedFeature<RelationMember>>();
		
		// Create an action dao but disable it so that no records will be written.
		actionDao = new ActionDao(dbCtx, false);
		
		userSet = new HashSet<Integer>();
		userDao = new UserDao(dbCtx, actionDao);
		
		nodeBuilder = new NodeMapper();
		wayBuilder = new WayMapper(enableBboxBuilder, enableLinestringBuilder);
		relationBuilder = new RelationMapper();
		nodeTagBuilder = new TagMapper(nodeBuilder.getEntityName());
		wayTagBuilder = new TagMapper(wayBuilder.getEntityName());
		relationTagBuilder = new TagMapper(relationBuilder.getEntityName());
		wayNodeBuilder = new WayNodeMapper();
		relationMemberBuilder = new RelationMemberMapper();
		wayGeometryBuilder = new WayGeometryBuilder(storeType);
		
		uncommittedEntityCount = 0;
		
		statementContainer = new ReleasableStatementContainer();
		
		initialized = false;
	}
	
	
	/**
	 * Initialises prepared statements and obtains database locks. Can be called
	 * multiple times.
	 */
	private void initialize() {
		if (!initialized) {
			schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
			bulkNodeStatement = statementContainer.add(
					dbCtx.prepareStatement(nodeBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_NODE)));
			singleNodeStatement = statementContainer.add(
					dbCtx.prepareStatement(nodeBuilder.getSqlInsert(1)));
			bulkNodeTagStatement = statementContainer.add(
					dbCtx.prepareStatement(nodeTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_NODE_TAG)));
			singleNodeTagStatement = statementContainer.add(
					dbCtx.prepareStatement(nodeTagBuilder.getSqlInsert(1)));
			bulkWayStatement = statementContainer.add(
					dbCtx.prepareStatement(wayBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY)));
			singleWayStatement = statementContainer.add(
					dbCtx.prepareStatement(wayBuilder.getSqlInsert(1)));
			bulkWayTagStatement = statementContainer.add(
					dbCtx.prepareStatement(wayTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY_TAG)));
			singleWayTagStatement = statementContainer.add(
					dbCtx.prepareStatement(wayTagBuilder.getSqlInsert(1)));
			bulkWayNodeStatement = statementContainer.add(
					dbCtx.prepareStatement(wayNodeBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY_NODE)));
			singleWayNodeStatement = statementContainer.add(
					dbCtx.prepareStatement(wayNodeBuilder.getSqlInsert(1)));
			bulkRelationStatement = statementContainer.add(
					dbCtx.prepareStatement(relationBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION)));
			singleRelationStatement = statementContainer.add(
					dbCtx.prepareStatement(relationBuilder.getSqlInsert(1)));
			bulkRelationTagStatement = statementContainer.add(
					dbCtx.prepareStatement(relationTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION_TAG)));
			singleRelationTagStatement = statementContainer.add(
					dbCtx.prepareStatement(relationTagBuilder.getSqlInsert(1)));
			bulkRelationMemberStatement = statementContainer.add(
					dbCtx.prepareStatement(relationMemberBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION_MEMBER)));
			singleRelationMemberStatement = statementContainer.add(
					dbCtx.prepareStatement(relationMemberBuilder.getSqlInsert(1)));
			
			// Drop all constraints and indexes.
			indexManager.prepareForLoad();
			
			LOG.fine("Loading data.");
			
			initialized = true;
		}
	}
	
	
	/**
	 * Writes the specified user to the database if it hasn't already been.
	 * 
	 * @param user
	 *            The user to add.
	 */
	private void writeUser(OsmUser user) {
		// Write the user to the database if it hasn't already been.
		if (!user.equals(OsmUser.NONE)) {
			if (!userSet.contains(user.getId())) {
				userDao.addUser(user);
				
				userSet.add(user.getId());
			}
		}
	}
	
	
	/**
	 * Flushes nodes to the database. If complete is false, this will only write
	 * nodes until the remaining node count is less than the multi-row insert
	 * statement row count. If complete is true, all remaining rows will be
	 * written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushNodes(boolean complete) {
		while (nodeBuffer.size() >= INSERT_BULK_ROW_COUNT_NODE) {
			List<Node> processedNodes;
			int prmIndex;
			
			processedNodes = new ArrayList<Node>(INSERT_BULK_ROW_COUNT_NODE);
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_NODE; i++) {
				Node node;
				
				node = nodeBuffer.remove(0);
				processedNodes.add(node);
				
				prmIndex = nodeBuilder.populateEntityParameters(bulkNodeStatement, prmIndex, node);
				
				uncommittedEntityCount++;
			}
			
			try {
				bulkNodeStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert nodes into the database.", e);
			}
			
			for (Node node : processedNodes) {
				addNodeTags(node);
			}
		}
		
		if (complete) {
			while (nodeBuffer.size() > 0) {
				Node node;
				
				node = nodeBuffer.remove(0);
				
				nodeBuilder.populateEntityParameters(singleNodeStatement, 1, node);
				
				uncommittedEntityCount++;
				
				try {
					singleNodeStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a node into the database.", e);
				}
				
				addNodeTags(node);
			}
		}
	}
	
	
	/**
	 * Process the node tags.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	private void addNodeTags(Node node) {
		for (Tag tag : node.getTags()) {
			nodeTagBuffer.add(new DbFeature<Tag>(node.getId(), tag));
		}
		
		flushNodeTags(false);
	}
	
	
	/**
	 * Flushes node tags to the database. If complete is false, this will only
	 * write node tags until the remaining node tag count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushNodeTags(boolean complete) {
		while (nodeTagBuffer.size() >= INSERT_BULK_ROW_COUNT_NODE_TAG) {
			int prmIndex;
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_NODE_TAG; i++) {
				prmIndex = nodeTagBuilder.populateEntityParameters(
						bulkNodeTagStatement, prmIndex, nodeTagBuffer.remove(0));
			}
			
			try {
				bulkNodeTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert node tags into the database.", e);
			}
		}
		
		if (complete) {
			while (nodeTagBuffer.size() > 0) {
				nodeTagBuilder.populateEntityParameters(singleNodeTagStatement, 1, nodeTagBuffer.remove(0));
				
				try {
					singleNodeTagStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a node tag into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes ways to the database. If complete is false, this will only write
	 * ways until the remaining way count is less than the multi-row insert
	 * statement row count. If complete is true, all remaining rows will be
	 * written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushWays(boolean complete) {
		while (wayBuffer.size() >= INSERT_BULK_ROW_COUNT_WAY) {
			List<Way> processedWays;
			int prmIndex;
			
			processedWays = new ArrayList<Way>(INSERT_BULK_ROW_COUNT_WAY);
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_WAY; i++) {
				Way way;
				List<Geometry> geometries;
				
				way = wayBuffer.remove(0);
				processedWays.add(way);
				
				geometries = new ArrayList<Geometry>();
				if (enableBboxBuilder) {
					geometries.add(wayGeometryBuilder.createWayBbox(way));
				}
				if (enableLinestringBuilder) {
					geometries.add(wayGeometryBuilder.createWayLinestring(way));
				}
				prmIndex = wayBuilder.populateEntityParameters(bulkWayStatement, prmIndex, way, geometries);
				
				uncommittedEntityCount++;
			}
			
			try {
				bulkWayStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert ways into the database.", e);
			}
			
			for (Way way : processedWays) {
				addWayTags(way);
				addWayNodes(way);
			}
		}
		
		if (complete) {
			while (wayBuffer.size() > 0) {
				Way way;
				List<Geometry> geometries;
				
				way = wayBuffer.remove(0);
				
				geometries = new ArrayList<Geometry>();
				if (enableBboxBuilder) {
					geometries.add(wayGeometryBuilder.createWayBbox(way));
				}
				if (enableLinestringBuilder) {
					geometries.add(wayGeometryBuilder.createWayLinestring(way));
				}
				wayBuilder.populateEntityParameters(singleWayStatement, 1, way, geometries);
				
				uncommittedEntityCount++;
				
				try {
					singleWayStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way into the database.", e);
				}
				
				addWayTags(way);
				addWayNodes(way);
			}
		}
	}
	
	
	/**
	 * Process the way tags.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWayTags(Way way) {
		for (Tag tag : way.getTags()) {
			wayTagBuffer.add(new DbFeature<Tag>(way.getId(), tag));
		}
		
		flushWayTags(false);
	}
	
	
	/**
	 * Process the way nodes.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWayNodes(Way way) {
		int sequenceId;
		
		sequenceId = 0;
		for (WayNode wayNode : way.getWayNodes()) {
			wayNodeBuffer.add(new DbOrderedFeature<WayNode>(way.getId(), wayNode, sequenceId++));
		}
		
		flushWayNodes(false);
	}
	
	
	/**
	 * Flushes way tags to the database. If complete is false, this will only
	 * write way tags until the remaining way tag count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushWayTags(boolean complete) {
		while (wayTagBuffer.size() >= INSERT_BULK_ROW_COUNT_WAY_TAG) {
			int prmIndex;
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_WAY_TAG; i++) {
				prmIndex = wayTagBuilder.populateEntityParameters(
						bulkWayTagStatement, prmIndex, wayTagBuffer.remove(0));
			}
			
			try {
				bulkWayTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert way tags into the database.", e);
			}
		}
		
		if (complete) {
			while (wayTagBuffer.size() > 0) {
				wayTagBuilder.populateEntityParameters(singleWayTagStatement, 1, wayTagBuffer.remove(0));
				
				try {
					singleWayTagStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way tag into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes way nodes to the database. If complete is false, this will only
	 * write way nodes until the remaining way node count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushWayNodes(boolean complete) {
		while (wayNodeBuffer.size() >= INSERT_BULK_ROW_COUNT_WAY_NODE) {
			int prmIndex;
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_WAY_NODE; i++) {
				prmIndex = wayNodeBuilder.populateEntityParameters(
						bulkWayNodeStatement, prmIndex, wayNodeBuffer.remove(0));
			}
			
			try {
				bulkWayNodeStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert way nodes into the database.", e);
			}
		}
		
		if (complete) {
			while (wayNodeBuffer.size() > 0) {
				wayNodeBuilder.populateEntityParameters(singleWayNodeStatement, 1, wayNodeBuffer.remove(0));
				
				try {
					singleWayNodeStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way node into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes relations to the database. If complete is false, this will only write
	 * relations until the remaining relation count is less than the multi-row insert
	 * statement row count. If complete is true, all remaining rows will be
	 * written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushRelations(boolean complete) {
		while (relationBuffer.size() >= INSERT_BULK_ROW_COUNT_RELATION) {
			List<Relation> processedRelations;
			int prmIndex;
			
			processedRelations = new ArrayList<Relation>(INSERT_BULK_ROW_COUNT_RELATION);
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_RELATION; i++) {
				Relation relation;
				
				relation = relationBuffer.remove(0);
				processedRelations.add(relation);
				
				prmIndex = relationBuilder.populateEntityParameters(bulkRelationStatement, prmIndex, relation);
				
				uncommittedEntityCount++;
			}
			
			try {
				bulkRelationStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relations into the database.", e);
			}
			
			for (Relation relation : processedRelations) {
				addRelationTags(relation);
				addRelationMembers(relation);
			}
		}
		
		if (complete) {
			while (relationBuffer.size() > 0) {
				Relation relation;
				
				relation = relationBuffer.remove(0);
				
				relationBuilder.populateEntityParameters(singleRelationStatement, 1, relation);
				
				uncommittedEntityCount++;
				
				try {
					singleRelationStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a relation into the database.", e);
				}
				
				addRelationTags(relation);
				addRelationMembers(relation);
			}
		}
	}
	
	
	/**
	 * Process the relation tags.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	private void addRelationTags(Relation relation) {
		for (Tag tag : relation.getTags()) {
			relationTagBuffer.add(new DbFeature<Tag>(relation.getId(), tag));
		}
		
		flushRelationTags(false);
	}
	
	
	/**
	 * Process the relation members.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	private void addRelationMembers(Relation relation) {
		int sequenceId;
		
		sequenceId = 0;
		for (RelationMember relationMember : relation.getMembers()) {
			relationMemberBuffer.add(
					new DbOrderedFeature<RelationMember>(relation.getId(), relationMember, sequenceId++));
		}
		
		flushRelationMembers(false);
	}
	
	
	/**
	 * Flushes relation tags to the database. If complete is false, this will only
	 * write relation tags until the remaining relation tag count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushRelationTags(boolean complete) {
		while (relationTagBuffer.size() >= INSERT_BULK_ROW_COUNT_RELATION_TAG) {
			int prmIndex;
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_RELATION_TAG; i++) {
				prmIndex = relationTagBuilder.populateEntityParameters(
						bulkRelationTagStatement, prmIndex, relationTagBuffer.remove(0));
			}
			
			try {
				bulkRelationTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relation tags into the database.", e);
			}
		}
		
		if (complete) {
			while (relationTagBuffer.size() > 0) {
				relationTagBuilder.populateEntityParameters(singleRelationTagStatement, 1, relationTagBuffer.remove(0));
				
				try {
					singleRelationTagStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a relation tag into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes relation members to the database. If complete is false, this will only
	 * write relation members until the remaining relation member count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushRelationMembers(boolean complete) {
		while (relationMemberBuffer.size() >= INSERT_BULK_ROW_COUNT_RELATION_MEMBER) {
			int prmIndex;
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_RELATION_MEMBER; i++) {
				prmIndex = relationMemberBuilder.populateEntityParameters(
						bulkRelationMemberStatement, prmIndex, relationMemberBuffer.remove(0));
			}
			
			try {
				bulkRelationMemberStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relation members into the database.", e);
			}
		}
		
		if (complete) {
			while (relationMemberBuffer.size() > 0) {
				relationMemberBuilder.populateEntityParameters(
						singleRelationMemberStatement, 1, relationMemberBuffer.remove(0));
				
				try {
					singleRelationMemberStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a relation member into the database.", e);
				}
			}
		}
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Do nothing.
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		initialize();
		
		LOG.fine("Flushing buffers.");
		
		flushNodes(true);
		flushNodeTags(true);
		flushWays(true);
		flushWayTags(true);
		flushWayNodes(true);
		flushRelations(true);
		flushRelationTags(true);
		flushRelationMembers(true);
		
		LOG.fine("Data load complete.");
		
		// Add all constraints and indexes.
		indexManager.completeAfterLoad();
		
		LOG.fine("Committing changes.");
		dbCtx.commit();
		
		LOG.fine("Vacuuming database.");
		dbCtx.setAutoCommit(true);
		dbCtx.executeStatement("VACUUM ANALYZE");
		
		LOG.fine("Complete.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		// Write the user to the database if required.
		writeUser(entityContainer.getEntity().getUser());
		
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer bound) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		if (enableBboxBuilder || enableLinestringBuilder) {
			wayGeometryBuilder.addNodeLocation(nodeContainer.getEntity());
		}
		
		nodeBuffer.add(nodeContainer.getEntity());
		
		flushNodes(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		// Ignore ways with a single node because they can't be loaded into postgis.
		if (wayContainer.getEntity().getWayNodes().size() > 1) {
			wayBuffer.add(wayContainer.getEntity());
		}
		
		flushWays(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		relationBuffer.add(relationContainer.getEntity());
		
		flushRelations(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
		wayGeometryBuilder.release();
		
		dbCtx.release();
	}
}
