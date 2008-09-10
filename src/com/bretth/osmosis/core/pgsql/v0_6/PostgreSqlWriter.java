// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.database.ReleasableStatementContainer;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityFeature;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBWayNode;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.pgsql.v0_6.impl.DatabaseCapabilityChecker;
import com.bretth.osmosis.core.pgsql.v0_6.impl.NodeBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.RelationBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.RelationMemberBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.TagBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.UserDao;
import com.bretth.osmosis.core.pgsql.v0_6.impl.WayBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.WayNodeBuilder;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriter implements Sink, EntityProcessor {
	
	private static final Logger log = Logger.getLogger(PostgreSqlWriter.class.getName());
	
	
	private static final String PRE_LOAD_SQL[] = {
		"ALTER TABLE users DROP CONSTRAINT pk_users",
		"ALTER TABLE nodes DROP CONSTRAINT pk_nodes",
		"ALTER TABLE ways DROP CONSTRAINT pk_ways",
		"ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes",
		"ALTER TABLE relations DROP CONSTRAINT pk_relations",
		"DROP INDEX idx_nodes_action",
		"DROP INDEX idx_node_tags_node_id",
		"DROP INDEX idx_nodes_geom",
		"DROP INDEX idx_ways_action",
		"DROP INDEX idx_way_tags_way_id",
		"DROP INDEX idx_relations_action",
		"DROP INDEX idx_relation_tags_relation_id"
	};
	private static final String PRE_LOAD_SQL_WAY_BBOX[] = {
		"DROP INDEX idx_ways_bbox"
	};
	
	private static final String POST_LOAD_SQL[] = {
		"ALTER TABLE ONLY users ADD CONSTRAINT pk_users PRIMARY KEY (id)",
		"ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id)",
		"ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id)",
		"ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id)",
		"ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id)",
		"CREATE INDEX idx_nodes_action ON nodes USING btree (action)",
		"CREATE INDEX idx_node_tags_node_id ON node_tags USING btree (node_id)",
		"CREATE INDEX idx_nodes_geom ON nodes USING gist (geom)",
		"CREATE INDEX idx_ways_action ON ways USING btree (action)",
		"CREATE INDEX idx_way_tags_way_id ON way_tags USING btree (way_id)",
		"CREATE INDEX idx_relations_action ON relations USING btree (action)",
		"CREATE INDEX idx_relation_tags_relation_id ON relation_tags USING btree (relation_id)"
	};
	private static final String POST_LOAD_SQL_WAY_BBOX[] = {
		"UPDATE ways SET bbox = (SELECT Envelope(Collect(geom)) FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id WHERE way_nodes.way_id = ways.id)",
		"CREATE INDEX idx_ways_bbox ON ways USING gist (bbox)"
	};
	
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
	
	/**
	 * Defines the number of entities to write between each commit.
	 */
	private static final int COMMIT_ENTITY_COUNT = -1;
	
	
	private DatabaseContext dbCtx;
	private DatabasePreferences preferences;
	private SchemaVersionValidator schemaVersionValidator;
	private DatabaseCapabilityChecker capabilityChecker;
	private List<Node> nodeBuffer;
	private List<DBEntityFeature<Tag>> nodeTagBuffer;
	private List<Way> wayBuffer;
	private List<DBEntityFeature<Tag>> wayTagBuffer;
	private List<DBWayNode> wayNodeBuffer;
	private List<Relation> relationBuffer;
	private List<DBEntityFeature<Tag>> relationTagBuffer;
	private List<DBEntityFeature<RelationMember>> relationMemberBuffer;
	private boolean initialized;
	private HashSet<Integer> userSet;
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
	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;
	private RelationBuilder relationBuilder;
	private TagBuilder nodeTagBuilder;
	private TagBuilder wayTagBuilder;
	private TagBuilder relationTagBuilder;
	private WayNodeBuilder wayNodeBuilder;
	private RelationMemberBuilder relationMemberBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		this.preferences = preferences;
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
		
		nodeBuffer = new ArrayList<Node>();
		nodeTagBuffer = new ArrayList<DBEntityFeature<Tag>>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<DBEntityFeature<Tag>>();
		wayNodeBuffer = new ArrayList<DBWayNode>();
		relationBuffer = new ArrayList<Relation>();
		relationTagBuffer = new ArrayList<DBEntityFeature<Tag>>();
		relationMemberBuffer = new ArrayList<DBEntityFeature<RelationMember>>();
		
		userSet = new HashSet<Integer>();
		userDao = new UserDao(dbCtx);
		
		nodeBuilder = new NodeBuilder();
		wayBuilder = new WayBuilder();
		relationBuilder = new RelationBuilder();
		nodeTagBuilder = new TagBuilder(nodeBuilder.getEntityName());
		wayTagBuilder = new TagBuilder(wayBuilder.getEntityName());
		relationTagBuilder = new TagBuilder(relationBuilder.getEntityName());
		wayNodeBuilder = new WayNodeBuilder();
		relationMemberBuilder = new RelationMemberBuilder();
		
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
			if (preferences.getValidateSchemaVersion()) {
				schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			}
			
			bulkNodeStatement = statementContainer.add(dbCtx.prepareStatement(nodeBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_NODE)));
			singleNodeStatement = statementContainer.add(dbCtx.prepareStatement(nodeBuilder.getSqlInsert(1)));
			bulkNodeTagStatement = statementContainer.add(dbCtx.prepareStatement(nodeTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_NODE_TAG)));
			singleNodeTagStatement = statementContainer.add(dbCtx.prepareStatement(nodeTagBuilder.getSqlInsert(1)));
			bulkWayStatement = statementContainer.add(dbCtx.prepareStatement(wayBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY)));
			singleWayStatement = statementContainer.add(dbCtx.prepareStatement(wayBuilder.getSqlInsert(1)));
			bulkWayTagStatement = statementContainer.add(dbCtx.prepareStatement(wayTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY_TAG)));
			singleWayTagStatement = statementContainer.add(dbCtx.prepareStatement(wayTagBuilder.getSqlInsert(1)));
			bulkWayNodeStatement = statementContainer.add(dbCtx.prepareStatement(wayNodeBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_WAY_NODE)));
			singleWayNodeStatement = statementContainer.add(dbCtx.prepareStatement(wayNodeBuilder.getSqlInsert(1)));
			bulkRelationStatement = statementContainer.add(dbCtx.prepareStatement(relationBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION)));
			singleRelationStatement = statementContainer.add(dbCtx.prepareStatement(relationBuilder.getSqlInsert(1)));
			bulkRelationTagStatement = statementContainer.add(dbCtx.prepareStatement(relationTagBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION_TAG)));
			singleRelationTagStatement = statementContainer.add(dbCtx.prepareStatement(relationTagBuilder.getSqlInsert(1)));
			bulkRelationMemberStatement = statementContainer.add(dbCtx.prepareStatement(relationMemberBuilder.getSqlInsert(INSERT_BULK_ROW_COUNT_RELATION_MEMBER)));
			singleRelationMemberStatement = statementContainer.add(dbCtx.prepareStatement(relationMemberBuilder.getSqlInsert(1)));
			
			// Drop all constraints and indexes.
			log.fine("Running pre-load SQL statements.");
			for (int i = 0; i < PRE_LOAD_SQL.length; i++) {
				log.finer("SQL: " + PRE_LOAD_SQL[i]);
				dbCtx.executeStatement(PRE_LOAD_SQL[i]);
			}
			log.fine("Pre-load SQL statements complete.");
			if (capabilityChecker.isWayBboxSupported()) {
				log.fine("Running pre-load bbox SQL statements.");
				for (int i = 0; i < PRE_LOAD_SQL_WAY_BBOX.length; i++) {
					log.finer("SQL: " + PRE_LOAD_SQL_WAY_BBOX[i]);
					dbCtx.executeStatement(PRE_LOAD_SQL_WAY_BBOX[i]);
				}
			}
			log.fine("Loading data.");
			
			initialized = true;
		}
	}
	
	
	/**
	 * Commits outstanding changes to the database if a threshold of uncommitted
	 * data has been reached. This regular interval commit is intended to
	 * improve performance on databases where large transactions hinder
	 * performance.
	 */
	private void performIntervalCommit() {
		if (COMMIT_ENTITY_COUNT >= 0 && uncommittedEntityCount >= COMMIT_ENTITY_COUNT) {
			System.err.println("Commit");
			dbCtx.commit();
			
			uncommittedEntityCount = 0;
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
		if (user != OsmUser.NONE) {
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
		
		performIntervalCommit();
	}
	
	
	/**
	 * Process the node tags.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	private void addNodeTags(Node node) {
		for (Tag tag : node.getTagList()) {
			nodeTagBuffer.add(new DBEntityFeature<Tag>(node.getId(), tag));
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
				prmIndex = nodeTagBuilder.populateEntityParameters(bulkNodeTagStatement, prmIndex, nodeTagBuffer.remove(0));
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
				
				way = wayBuffer.remove(0);
				processedWays.add(way);
				
				prmIndex = wayBuilder.populateEntityParameters(bulkWayStatement, prmIndex, way);
				
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
				
				way = wayBuffer.remove(0);
				
				wayBuilder.populateEntityParameters(singleWayStatement, 1, way);
				
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
		
		performIntervalCommit();
	}
	
	
	/**
	 * Process the way tags.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWayTags(Way way) {
		for (Tag tag : way.getTagList()) {
			wayTagBuffer.add(new DBEntityFeature<Tag>(way.getId(), tag));
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
		for (WayNode wayNode : way.getWayNodeList()) {
			wayNodeBuffer.add(new DBWayNode(way.getId(), wayNode, sequenceId++));
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
				prmIndex = wayTagBuilder.populateEntityParameters(bulkWayTagStatement, prmIndex, wayTagBuffer.remove(0));
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
				prmIndex = wayNodeBuilder.populateEntityParameters(bulkWayNodeStatement, prmIndex, wayNodeBuffer.remove(0));
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
		
		performIntervalCommit();
	}
	
	
	/**
	 * Process the relation tags.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	private void addRelationTags(Relation relation) {
		for (Tag tag : relation.getTagList()) {
			relationTagBuffer.add(new DBEntityFeature<Tag>(relation.getId(), tag));
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
		for (RelationMember relationMember : relation.getMemberList()) {
			relationMemberBuffer.add(new DBEntityFeature<RelationMember>(relation.getId(), relationMember));
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
				prmIndex = relationTagBuilder.populateEntityParameters(bulkRelationTagStatement, prmIndex, relationTagBuffer.remove(0));
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
				prmIndex = relationMemberBuilder.populateEntityParameters(bulkRelationMemberStatement, prmIndex, relationMemberBuffer.remove(0));
			}
			
			try {
				bulkRelationMemberStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relation members into the database.", e);
			}
		}
		
		if (complete) {
			while (relationMemberBuffer.size() > 0) {
				relationMemberBuilder.populateEntityParameters(singleRelationMemberStatement, 1, relationMemberBuffer.remove(0));
				
				try {
					singleRelationMemberStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a relation member into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		initialize();
		
		log.fine("Flushing buffers.");
		
		flushNodes(true);
		flushNodeTags(true);
		flushWays(true);
		flushWayTags(true);
		flushWayNodes(true);
		flushRelations(true);
		flushRelationTags(true);
		flushRelationMembers(true);
		
		log.fine("Data load complete.");
		
		// Add all constraints and indexes.
		log.fine("Running post-load SQL.");
		for (int i = 0; i < POST_LOAD_SQL.length; i++) {
			log.finer("SQL: " + POST_LOAD_SQL[i]);
			dbCtx.executeStatement(POST_LOAD_SQL[i]);
		}
		if (capabilityChecker.isWayBboxSupported()) {
			log.fine("Running post-load bbox SQL statements.");
			for (int i = 0; i < POST_LOAD_SQL_WAY_BBOX.length; i++) {
				log.finer("SQL: " + POST_LOAD_SQL_WAY_BBOX[i]);
				dbCtx.executeStatement(POST_LOAD_SQL_WAY_BBOX[i]);
			}
		}
		log.fine("Post-load SQL complete.");
		
		dbCtx.commit();
		
		log.fine("Commit complete");
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
		nodeBuffer.add(nodeContainer.getEntity());
		
		flushNodes(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		// Ignore ways with a single node because they can't be loaded into postgis.
		if (wayContainer.getEntity().getWayNodeList().size() > 1) {
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
		
		dbCtx.release();
	}
}
