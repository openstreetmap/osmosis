// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_6.impl.ChangesetManager;
import com.bretth.osmosis.core.mysql.v0_6.impl.DbFeature;
import com.bretth.osmosis.core.mysql.v0_6.impl.DbOrderedFeature;
import com.bretth.osmosis.core.mysql.v0_6.impl.DbFeatureHistory;
import com.bretth.osmosis.core.mysql.v0_6.impl.MemberTypeRenderer;
import com.bretth.osmosis.core.mysql.v0_6.impl.UserManager;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class MysqlWriter implements Sink, EntityProcessor {
	// These SQL strings are the prefix to statements that will be built based
	// on how many rows of data are to be inserted at a time.
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes(id, timestamp, version, visible, changeset_id, latitude, longitude, tile)";
	private static final int INSERT_PRM_COUNT_NODE = 8;
	private static final String INSERT_SQL_NODE_TAG =
		"INSERT INTO node_tags (id, k, v, version)";
	private static final int INSERT_PRM_COUNT_NODE_TAG = 4;
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, timestamp, version, visible, changeset_id)";
	private static final int INSERT_PRM_COUNT_WAY = 5;
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags (id, k, v, version)";
	private static final int INSERT_PRM_COUNT_WAY_TAG = 4;
	private static final String INSERT_SQL_WAY_NODE =
		"INSERT INTO way_nodes (id, node_id, sequence_id, version)";
	private static final int INSERT_PRM_COUNT_WAY_NODE = 4;
	private static final String INSERT_SQL_RELATION =
		"INSERT INTO relations (id, timestamp, version, visible, changeset_id)";
	private static final int INSERT_PRM_COUNT_RELATION = 5;
	private static final String INSERT_SQL_RELATION_TAG =
		"INSERT INTO relation_tags (id, k, v, version)";
	private static final int INSERT_PRM_COUNT_RELATION_TAG = 4;
	private static final String INSERT_SQL_RELATION_MEMBER =
		"INSERT INTO relation_members (id, member_type, member_id, sequence_id, member_role, version)";
	private static final int INSERT_PRM_COUNT_RELATION_MEMBER = 6;
	
	// These SQL statements will be invoked prior to loading data to disable
	// indexes.
	private static final String[] INVOKE_DISABLE_KEYS = {
		"ALTER TABLE nodes DISABLE KEYS",
		"ALTER TABLE node_tags DISABLE KEYS",
		"ALTER TABLE ways DISABLE KEYS",
		"ALTER TABLE way_tags DISABLE KEYS",
		"ALTER TABLE way_nodes DISABLE KEYS",
		"ALTER TABLE relations DISABLE KEYS",
		"ALTER TABLE relation_tags DISABLE KEYS",
		"ALTER TABLE relation_members DISABLE KEYS"
	};
	
	// These SQL statements will be invoked after loading data to re-enable
	// indexes.
	private static final String[] INVOKE_ENABLE_KEYS = {
		"ALTER TABLE nodes ENABLE KEYS",
		"ALTER TABLE node_tags ENABLE KEYS",
		"ALTER TABLE ways ENABLE KEYS",
		"ALTER TABLE way_tags ENABLE KEYS",
		"ALTER TABLE way_nodes ENABLE KEYS",
		"ALTER TABLE relations ENABLE KEYS",
		"ALTER TABLE relation_tags ENABLE KEYS",
		"ALTER TABLE relation_members ENABLE KEYS"
	};
	
	// These SQL statements will be invoked after loading history tables to
	// populate the current tables.
	private static final int LOAD_CURRENT_NODE_ROW_COUNT = 1000000;
	private static final int LOAD_CURRENT_WAY_ROW_COUNT = 100000; // There are many node and tag records per way.
	private static final int LOAD_CURRENT_RELATION_ROW_COUNT = 100000; // There are many member and tag records per relation.
	private static final String LOAD_CURRENT_NODES =
		"INSERT INTO current_nodes SELECT id, latitude, longitude, changeset_id, visible, timestamp, tile, version FROM nodes WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_NODE_TAGS =
		"INSERT INTO current_node_tags SELECT id, k, v FROM node_tags WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAYS =
		"INSERT INTO current_ways SELECT id, changeset_id, timestamp, visible, version FROM ways WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAY_TAGS =
		"INSERT INTO current_way_tags SELECT id, k, v FROM way_tags WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAY_NODES =
		"INSERT INTO current_way_nodes SELECT id, node_id, sequence_id FROM way_nodes WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_RELATIONS =
		"INSERT INTO current_relations SELECT id, changeset_id, timestamp, visible, version FROM relations WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_RELATION_TAGS =
		"INSERT INTO current_relation_tags SELECT id, k, v FROM relation_tags WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_RELATION_MEMBERS =
		"INSERT INTO current_relation_members SELECT id, member_type, member_id, member_role, sequence_id FROM relation_members WHERE id >= ? AND id < ?";
	
	// These SQL statements will be invoked to lock and unlock tables.
	private static final String INVOKE_LOCK_TABLES =
		"LOCK TABLES"
		+ " nodes WRITE, node_tags WRITE,"
		+ " ways WRITE, way_tags WRITE, way_nodes WRITE,"
		+ " relations WRITE, relation_tags WRITE, relation_members WRITE,"
		+ " current_nodes WRITE, current_node_tags WRITE,"
		+ " current_ways WRITE, current_way_tags WRITE, current_way_nodes WRITE,"
		+ " current_relations WRITE, current_relation_tags WRITE, current_relation_members WRITE,"
		+ " users WRITE, changesets WRITE";
	private static final String INVOKE_UNLOCK_TABLES = "UNLOCK TABLES";
	
	// These constants define how many rows of each data type will be inserted
	// with single insert statements.
	private static final int INSERT_BULK_ROW_COUNT_NODE = 100;
	private static final int INSERT_BULK_ROW_COUNT_NODE_TAG = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_TAG = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_NODE = 100;
	private static final int INSERT_BULK_ROW_COUNT_RELATION = 100;
	private static final int INSERT_BULK_ROW_COUNT_RELATION_TAG = 100;
	private static final int INSERT_BULK_ROW_COUNT_RELATION_MEMBER = 100;
	
	// These constants will be configured by a static code block.
	private static final String INSERT_SQL_SINGLE_NODE;
	private static final String INSERT_SQL_BULK_NODE;
	private static final String INSERT_SQL_SINGLE_NODE_TAG;
	private static final String INSERT_SQL_BULK_NODE_TAG;
	private static final String INSERT_SQL_SINGLE_WAY;
	private static final String INSERT_SQL_BULK_WAY;
	private static final String INSERT_SQL_SINGLE_WAY_TAG;
	private static final String INSERT_SQL_BULK_WAY_TAG;
	private static final String INSERT_SQL_SINGLE_WAY_NODE;
	private static final String INSERT_SQL_BULK_WAY_NODE;
	private static final String INSERT_SQL_SINGLE_RELATION;
	private static final String INSERT_SQL_BULK_RELATION;
	private static final String INSERT_SQL_SINGLE_RELATION_TAG;
	private static final String INSERT_SQL_BULK_RELATION_TAG;
	private static final String INSERT_SQL_SINGLE_RELATION_MEMBER;
	private static final String INSERT_SQL_BULK_RELATION_MEMBER;
	
	
	/**
	 * Builds a multi-row SQL insert statement.
	 * 
	 * @param baseSql
	 *            The basic query without value bind variables.
	 * @param parameterCount
	 *            The number of parameters to be inserted.
	 * @param rowCount
	 *            The number of rows to insert in a single query.
	 * @return The generated SQL statement.
	 */
	private static String buildSqlInsertStatement(String baseSql, int parameterCount, int rowCount) {
		StringBuilder buffer;
		
		buffer = new StringBuilder();
		
		buffer.append(baseSql).append(" VALUES ");
		
		for (int i = 0; i < rowCount; i++) {
			if (i > 0) {
				buffer.append(", ");
			}
			buffer.append("(");
			
			for (int j = 0; j < parameterCount; j++) {
				if (j > 0) {
					buffer.append(", ");
				}
				
				buffer.append("?");
			}
			
			buffer.append(")");
		}
		
		return buffer.toString();
	}
	
	
	static {
		INSERT_SQL_SINGLE_NODE =
			buildSqlInsertStatement(INSERT_SQL_NODE, INSERT_PRM_COUNT_NODE, 1);
		INSERT_SQL_BULK_NODE =
			buildSqlInsertStatement(INSERT_SQL_NODE, INSERT_PRM_COUNT_NODE, INSERT_BULK_ROW_COUNT_NODE);
		INSERT_SQL_SINGLE_NODE_TAG =
			buildSqlInsertStatement(INSERT_SQL_NODE_TAG, INSERT_PRM_COUNT_NODE_TAG, 1);
		INSERT_SQL_BULK_NODE_TAG =
			buildSqlInsertStatement(INSERT_SQL_NODE_TAG, INSERT_PRM_COUNT_NODE_TAG, INSERT_BULK_ROW_COUNT_NODE_TAG);
		INSERT_SQL_SINGLE_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, 1);
		INSERT_SQL_BULK_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, INSERT_BULK_ROW_COUNT_WAY);
		INSERT_SQL_SINGLE_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, 1);
		INSERT_SQL_BULK_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, INSERT_BULK_ROW_COUNT_WAY_TAG);
		INSERT_SQL_SINGLE_WAY_NODE =
			buildSqlInsertStatement(INSERT_SQL_WAY_NODE, INSERT_PRM_COUNT_WAY_NODE, 1);
		INSERT_SQL_BULK_WAY_NODE =
			buildSqlInsertStatement(INSERT_SQL_WAY_NODE, INSERT_PRM_COUNT_WAY_NODE, INSERT_BULK_ROW_COUNT_WAY_NODE);
		INSERT_SQL_SINGLE_RELATION =
			buildSqlInsertStatement(INSERT_SQL_RELATION, INSERT_PRM_COUNT_RELATION, 1);
		INSERT_SQL_BULK_RELATION =
			buildSqlInsertStatement(INSERT_SQL_RELATION, INSERT_PRM_COUNT_RELATION, INSERT_BULK_ROW_COUNT_RELATION);
		INSERT_SQL_SINGLE_RELATION_TAG =
			buildSqlInsertStatement(INSERT_SQL_RELATION_TAG, INSERT_PRM_COUNT_RELATION_TAG, 1);
		INSERT_SQL_BULK_RELATION_TAG =
			buildSqlInsertStatement(INSERT_SQL_RELATION_TAG, INSERT_PRM_COUNT_RELATION_TAG, INSERT_BULK_ROW_COUNT_RELATION_TAG);
		INSERT_SQL_SINGLE_RELATION_MEMBER =
			buildSqlInsertStatement(INSERT_SQL_RELATION_MEMBER, INSERT_PRM_COUNT_RELATION_MEMBER, 1);
		INSERT_SQL_BULK_RELATION_MEMBER =
			buildSqlInsertStatement(INSERT_SQL_RELATION_MEMBER, INSERT_PRM_COUNT_RELATION_MEMBER, INSERT_BULK_ROW_COUNT_RELATION_MEMBER);
	}
	
	
	private DatabasePreferences preferences;
	private DatabaseContext dbCtx;
	private UserManager userManager;
	private ChangesetManager changesetManager;
	private SchemaVersionValidator schemaVersionValidator;
	private boolean lockTables;
	private boolean populateCurrentTables;
	private List<Node> nodeBuffer;
	private List<DbFeatureHistory<DbFeature<Tag>>> nodeTagBuffer;
	private List<Way> wayBuffer;
	private List<DbFeatureHistory<DbFeature<Tag>>> wayTagBuffer;
	private List<DbFeatureHistory<DbOrderedFeature<WayNode>>> wayNodeBuffer;
	private List<Relation> relationBuffer;
	private List<DbFeatureHistory<DbFeature<Tag>>> relationTagBuffer;
	private List<DbFeatureHistory<DbOrderedFeature<RelationMember>>> relationMemberBuffer;
	private long maxNodeId;
	private long maxWayId;
	private long maxRelationId;
	private TileCalculator tileCalculator;
	private MemberTypeRenderer memberTypeRenderer;
	private boolean initialized;
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
	private PreparedStatement loadCurrentNodesStatement;
	private PreparedStatement loadCurrentNodeTagsStatement;
	private PreparedStatement loadCurrentWaysStatement;
	private PreparedStatement loadCurrentWayTagsStatement;
	private PreparedStatement loadCurrentWayNodesStatement;
	private PreparedStatement loadCurrentRelationsStatement;
	private PreparedStatement loadCurrentRelationTagsStatement;
	private PreparedStatement loadCurrentRelationMembersStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param lockTables
	 *            If true, all tables will be locked during loading.
	 * @param populateCurrentTables
	 *            If true, the current tables will be populated as well as
	 *            history tables.
	 */
	public MysqlWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean lockTables, boolean populateCurrentTables) {
		this.preferences = preferences;
		
		dbCtx = new DatabaseContext(loginCredentials);
		
		userManager = new UserManager(dbCtx);
		changesetManager = new ChangesetManager(dbCtx);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
		
		this.lockTables = lockTables;
		this.populateCurrentTables = populateCurrentTables;
		
		nodeBuffer = new ArrayList<Node>();
		nodeTagBuffer = new ArrayList<DbFeatureHistory<DbFeature<Tag>>>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<DbFeatureHistory<DbFeature<Tag>>>();
		wayNodeBuffer = new ArrayList<DbFeatureHistory<DbOrderedFeature<WayNode>>>();
		relationBuffer = new ArrayList<Relation>();
		relationTagBuffer = new ArrayList<DbFeatureHistory<DbFeature<Tag>>>();
		relationMemberBuffer = new ArrayList<DbFeatureHistory<DbOrderedFeature<RelationMember>>>();
		
		maxNodeId = 0;
		maxWayId = 0;
		maxRelationId = 0;
		
		tileCalculator = new TileCalculator();
		memberTypeRenderer = new MemberTypeRenderer();
		
		initialized = false;
	}
	
	
	/**
	 * Initialises prepared statements and obtains database locks. Can be called
	 * multiple times.
	 */
	private void initialize() {
		if (!initialized) {
			if (preferences.getValidateSchemaVersion()) {
				schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
			}
			
			bulkNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE);
			singleNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE);
			bulkNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE_TAG);
			singleNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE_TAG);
			bulkWayStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY);
			singleWayStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY);
			bulkWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_TAG);
			singleWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_TAG);
			bulkWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_NODE);
			singleWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_NODE);
			bulkRelationStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_RELATION);
			singleRelationStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_RELATION);
			bulkRelationTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_RELATION_TAG);
			singleRelationTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_RELATION_TAG);
			bulkRelationMemberStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_RELATION_MEMBER);
			singleRelationMemberStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_RELATION_MEMBER);
			
			loadCurrentNodesStatement = dbCtx.prepareStatement(LOAD_CURRENT_NODES);
			loadCurrentNodeTagsStatement = dbCtx.prepareStatement(LOAD_CURRENT_NODE_TAGS);
			loadCurrentWaysStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAYS);
			loadCurrentWayTagsStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAY_TAGS);
			loadCurrentWayNodesStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAY_NODES);
			loadCurrentRelationsStatement = dbCtx.prepareStatement(LOAD_CURRENT_RELATIONS);
			loadCurrentRelationTagsStatement = dbCtx.prepareStatement(LOAD_CURRENT_RELATION_TAGS);
			loadCurrentRelationMembersStatement = dbCtx.prepareStatement(LOAD_CURRENT_RELATION_MEMBERS);
			
			// Disable indexes to improve load performance.
			for (int i = 0; i < INVOKE_DISABLE_KEYS.length; i++) {
				dbCtx.executeStatement(INVOKE_DISABLE_KEYS[i]);
			}
			
			// Lock tables if required to improve load performance.
			if (lockTables) {
				dbCtx.executeStatement(INVOKE_LOCK_TABLES);
			}
			
			initialized = true;
		}
	}
	
	
	/**
	 * Sets node values as bind variable parameters to a node insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param node
	 *            The node containing the data to be inserted.
	 */
	private void populateNodeParameters(PreparedStatement statement, int initialIndex, Node node) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		// We can't write an entity with a null timestamp.
		if (node.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Node " + node.getId() + " does not have a timestamp set.");
		}
		
		try {
			statement.setLong(prmIndex++, node.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			statement.setInt(prmIndex++, node.getVersion());
			statement.setBoolean(prmIndex++, true);
			statement.setLong(prmIndex++, changesetManager.obtainChangesetId(node.getUser()));
			statement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
			statement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
			statement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a node.", e);
		}
	}
	
	
	/**
	 * Sets way values as bind variable parameters to a way insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param way
	 *            The way containing the data to be inserted.
	 */
	private void populateWayParameters(PreparedStatement statement, int initialIndex, Way way) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		// We can't write an entity with a null timestamp.
		if (way.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Way " + way.getId() + " does not have a timestamp set.");
		}
		
		try {
			statement.setLong(prmIndex++, way.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
			statement.setInt(prmIndex++, way.getVersion());
			statement.setBoolean(prmIndex++, true);
			statement.setLong(prmIndex++, changesetManager.obtainChangesetId(way.getUser()));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way.", e);
		}
	}
	
	
	/**
	 * Sets tag values as bind variable parameters to a tag insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param dbEntityTag
	 *            The entity tag containing the data to be inserted.
	 */
	private void populateEntityTagParameters(PreparedStatement statement, int initialIndex, DbFeatureHistory<DbFeature<Tag>> dbEntityTag) {
		int prmIndex;
		Tag tag;
		
		prmIndex = initialIndex;
		
		tag = dbEntityTag.getDbFeature().getFeature();
		
		try {
			statement.setLong(prmIndex++, dbEntityTag.getDbFeature().getEntityId());
			statement.setString(prmIndex++, tag.getKey());
			statement.setString(prmIndex++, tag.getValue());
			statement.setInt(prmIndex++, dbEntityTag.getVersion());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for an entity tag.", e);
		}
	}
	
	
	/**
	 * Sets node reference values as bind variable parameters to a way node insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param dbWayNode
	 *            The way node containing the data to be inserted.
	 */
	private void populateWayNodeParameters(PreparedStatement statement, int initialIndex, DbFeatureHistory<DbOrderedFeature<WayNode>> dbWayNode) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, dbWayNode.getDbFeature().getEntityId());
			statement.setLong(prmIndex++, dbWayNode.getDbFeature().getFeature().getNodeId());
			statement.setInt(prmIndex++, dbWayNode.getDbFeature().getSequenceId());
			statement.setInt(prmIndex++, dbWayNode.getVersion());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way node.", e);
		}
	}
	
	
	/**
	 * Sets relation values as bind variable parameters to a relation insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param relation
	 *            The way containing the data to be inserted.
	 */
	private void populateRelationParameters(PreparedStatement statement, int initialIndex, Relation relation) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		// We can't write an entity with a null timestamp.
		if (relation.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Relation " + relation.getId() + " does not have a timestamp set.");
		}
		
		try {
			statement.setLong(prmIndex++, relation.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
			statement.setInt(prmIndex++, relation.getVersion());
			statement.setBoolean(prmIndex++, true);
			statement.setLong(prmIndex++, changesetManager.obtainChangesetId(relation.getUser()));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a relation.", e);
		}
	}
	
	
	/**
	 * Sets member reference values as bind variable parameters to a relation
	 * member insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param dbRelationMember
	 *            The relation member containing the data to be inserted.
	 */
	private void populateRelationMemberParameters(PreparedStatement statement, int initialIndex, DbFeatureHistory<DbOrderedFeature<RelationMember>> dbRelationMember) {
		int prmIndex;
		RelationMember relationMember;
		
		prmIndex = initialIndex;
		
		relationMember = dbRelationMember.getDbFeature().getFeature();
		
		try {
			statement.setLong(prmIndex++, dbRelationMember.getDbFeature().getEntityId());
			statement.setString(prmIndex++, memberTypeRenderer.render(relationMember.getMemberType()));
			statement.setLong(prmIndex++, relationMember.getMemberId());
			statement.setInt(prmIndex++, dbRelationMember.getDbFeature().getSequenceId());
			statement.setString(prmIndex++, relationMember.getMemberRole());
			statement.setInt(prmIndex++, dbRelationMember.getVersion());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a relation member.", e);
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
			int prmIndex;
			List<Node> processedNodes;
			
			processedNodes = new ArrayList<Node>(INSERT_BULK_ROW_COUNT_NODE);
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_NODE; i++) {
				Node node;
				
				node = nodeBuffer.remove(0);
				processedNodes.add(node);
				
				populateNodeParameters(bulkNodeStatement, prmIndex, node);
				prmIndex += INSERT_PRM_COUNT_NODE;
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
				
				populateNodeParameters(singleNodeStatement, 1, node);
				
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
				populateEntityTagParameters(bulkNodeTagStatement, prmIndex, nodeTagBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_NODE_TAG;
			}
			
			try {
				bulkNodeTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert node tags into the database.", e);
			}
		}
		
		if (complete) {
			while (nodeTagBuffer.size() > 0) {
				populateEntityTagParameters(singleNodeTagStatement, 1, nodeTagBuffer.remove(0));
				
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
				
				populateWayParameters(bulkWayStatement, prmIndex, way);
				prmIndex += INSERT_PRM_COUNT_WAY;
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
				
				populateWayParameters(singleWayStatement, 1, way);
				
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
				populateEntityTagParameters(bulkWayTagStatement, prmIndex, wayTagBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_WAY_TAG;
			}
			
			try {
				bulkWayTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert way tags into the database.", e);
			}
		}
		
		if (complete) {
			while (wayTagBuffer.size() > 0) {
				populateEntityTagParameters(singleWayTagStatement, 1, wayTagBuffer.remove(0));
				
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
				populateWayNodeParameters(bulkWayNodeStatement, prmIndex, wayNodeBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_WAY_NODE;
			}
			
			try {
				bulkWayNodeStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert way nodes into the database.", e);
			}
		}
		
		if (complete) {
			while (wayNodeBuffer.size() > 0) {
				populateWayNodeParameters(singleWayNodeStatement, 1, wayNodeBuffer.remove(0));
				
				try {
					singleWayNodeStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way node into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes relations to the database. If complete is false, this will only
	 * write relations until the remaining way count is less than the multi-row
	 * insert statement row count. If complete is true, all remaining rows will
	 * be written using single row insert statements.
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
				
				populateRelationParameters(bulkRelationStatement, prmIndex, relation);
				prmIndex += INSERT_PRM_COUNT_RELATION;
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
				
				populateRelationParameters(singleRelationStatement, 1, relation);
				
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
	 * Flushes relation tags to the database. If complete is false, this will
	 * only write relation tags until the remaining relation tag count is less
	 * than the multi-row insert statement row count. If complete is true, all
	 * remaining rows will be written using single row insert statements.
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
				populateEntityTagParameters(bulkRelationTagStatement, prmIndex, relationTagBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_RELATION_TAG;
			}
			
			try {
				bulkRelationTagStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relation tags into the database.", e);
			}
		}
		
		if (complete) {
			while (relationTagBuffer.size() > 0) {
				populateEntityTagParameters(singleRelationTagStatement, 1, relationTagBuffer.remove(0));
				
				try {
					singleRelationTagStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a relation tag into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Flushes relation members to the database. If complete is false, this will
	 * only write relation members until the remaining relation member count is
	 * less than the multi-row insert statement row count. If complete is true,
	 * all remaining rows will be written using single row insert statements.
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
				populateRelationMemberParameters(bulkRelationMemberStatement, prmIndex, relationMemberBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_RELATION_MEMBER;
			}
			
			try {
				bulkRelationMemberStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert relation members into the database.", e);
			}
		}
		
		if (complete) {
			while (relationMemberBuffer.size() > 0) {
				populateRelationMemberParameters(singleRelationMemberStatement, 1, relationMemberBuffer.remove(0));
				
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
		
		flushNodes(true);
		flushNodeTags(true);
		flushWays(true);
		flushWayTags(true);
		flushWayNodes(true);
		flushRelations(true);
		flushRelationTags(true);
		flushRelationMembers(true);
		
		// Re-enable indexes now that the load has completed.
		for (int i = 0; i < INVOKE_DISABLE_KEYS.length; i++) {
			dbCtx.executeStatement(INVOKE_ENABLE_KEYS[i]);
		}
		
		if (populateCurrentTables) {
			// Copy data into the current node tables.
			for (int i = 0; i < maxNodeId; i += LOAD_CURRENT_NODE_ROW_COUNT) {
				// Node
				try {
					loadCurrentNodesStatement.setInt(1, i);
					loadCurrentNodesStatement.setInt(2, i + LOAD_CURRENT_NODE_ROW_COUNT);
					
					loadCurrentNodesStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current nodes.", e);
				}
				
				// Node tags
				try {
					loadCurrentNodeTagsStatement.setInt(1, i);
					loadCurrentNodeTagsStatement.setInt(2, i + LOAD_CURRENT_NODE_ROW_COUNT);
					
					loadCurrentNodeTagsStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current node tags.", e);
				}
				
				dbCtx.commit();
			}
			for (int i = 0; i < maxWayId; i += LOAD_CURRENT_WAY_ROW_COUNT) {
				// Way
				try {
					loadCurrentWaysStatement.setInt(1, i);
					loadCurrentWaysStatement.setInt(2, i + LOAD_CURRENT_WAY_ROW_COUNT);
					
					loadCurrentWaysStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current ways.", e);
				}
				
				// Way tags
				try {
					loadCurrentWayTagsStatement.setInt(1, i);
					loadCurrentWayTagsStatement.setInt(2, i + LOAD_CURRENT_WAY_ROW_COUNT);
					
					loadCurrentWayTagsStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current way tags.", e);
				}
				
				// Way nodes
				try {
					loadCurrentWayNodesStatement.setInt(1, i);
					loadCurrentWayNodesStatement.setInt(2, i + LOAD_CURRENT_WAY_ROW_COUNT);
					
					loadCurrentWayNodesStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current way nodes.", e);
				}
				
				dbCtx.commit();
			}
			for (int i = 0; i < maxRelationId; i += LOAD_CURRENT_RELATION_ROW_COUNT) {
				// Way
				try {
					loadCurrentRelationsStatement.setInt(1, i);
					loadCurrentRelationsStatement.setInt(2, i + LOAD_CURRENT_RELATION_ROW_COUNT);
					
					loadCurrentRelationsStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current relations.", e);
				}
				
				// Relation tags
				try {
					loadCurrentRelationTagsStatement.setInt(1, i);
					loadCurrentRelationTagsStatement.setInt(2, i + LOAD_CURRENT_RELATION_ROW_COUNT);
					
					loadCurrentRelationTagsStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current relation tags.", e);
				}
				
				// Relation members
				try {
					loadCurrentRelationMembersStatement.setInt(1, i);
					loadCurrentRelationMembersStatement.setInt(2, i + LOAD_CURRENT_RELATION_ROW_COUNT);
					
					loadCurrentRelationMembersStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current relation members.", e);
				}
				
				dbCtx.commit();
			}
		}
		
		// Unlock tables (if they were locked) now that we have completed.
		if (lockTables) {
			dbCtx.executeStatement(INVOKE_UNLOCK_TABLES);
		}
		
		dbCtx.commit();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		userManager.release();
		
		dbCtx.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		userManager.addOrUpdateUser(entityContainer.getEntity().getUser());
		
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		Node node;
		long nodeId;
		
		node = nodeContainer.getEntity();
		nodeId = node.getId();
		
		if (nodeId >= maxNodeId) {
			maxNodeId = nodeId + 1;
		}
		
		nodeBuffer.add(node);
		
		flushNodes(false);
	}
	
	
	/**
	 * Process the node tags.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	private void addNodeTags(Node node) {
		for (Tag tag: node.getTagList()) {
			nodeTagBuffer.add(new DbFeatureHistory<DbFeature<Tag>>(new DbFeature<Tag>(node.getId(), tag), node.getVersion()));
		}
		
		flushNodeTags(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way;
		long wayId;
		
		flushNodes(true);
		
		way = wayContainer.getEntity();
		wayId = way.getId();
		
		if (wayId >= maxWayId) {
			maxWayId = wayId + 1;
		}
		
		wayBuffer.add(way);
		
		flushWays(false);
	}
	
	
	/**
	 * Process the way tags.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWayTags(Way way) {
		for (Tag tag: way.getTagList()) {
			wayTagBuffer.add(new DbFeatureHistory<DbFeature<Tag>>(new DbFeature<Tag>(way.getId(), tag), way.getVersion()));
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
		List<WayNode> nodeReferenceList;
		
		nodeReferenceList = way.getWayNodeList();
		
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			wayNodeBuffer.add(
				new DbFeatureHistory<DbOrderedFeature<WayNode>>(
					new DbOrderedFeature<WayNode>(
						way.getId(),
						nodeReferenceList.get(i),
						i + 1
					),
					way.getVersion()
				)
			);
		}
		
		flushWayNodes(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		long relationId;
		
		flushWays(true);
		
		relation = relationContainer.getEntity();
		relationId = relation.getId();
		
		if (relationId >= maxRelationId) {
			maxRelationId = relationId + 1;
		}
		
		relationBuffer.add(relation);
		
		flushRelations(false);
	}
	
	
	/**
	 * Process the relation tags.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	private void addRelationTags(Relation relation) {
		for (Tag tag: relation.getTagList()) {
			relationTagBuffer.add(new DbFeatureHistory<DbFeature<Tag>>(new DbFeature<Tag>(relation.getId(), tag), relation.getVersion()));
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
		List<RelationMember> memberReferenceList;
		
		memberReferenceList = relation.getMemberList();
		
		for (int i = 0; i < memberReferenceList.size(); i++) {
			relationMemberBuffer.add(
				new DbFeatureHistory<DbOrderedFeature<RelationMember>>(
					new DbOrderedFeature<RelationMember>(
						relation.getId(),
						memberReferenceList.get(i),
						i + 1
					),
					relation.getVersion()
				)
			);
		}
		
		flushRelationMembers(false);
	}
}
