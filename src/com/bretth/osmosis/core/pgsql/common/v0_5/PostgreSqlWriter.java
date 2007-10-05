package com.bretth.osmosis.core.pgsql.common.v0_5;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.geometric.PGpoint;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.pgsql.common.UserIdManager;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriter implements Sink, EntityProcessor {
	// These SQL strings are the prefix to statements that will be built based
	// on how many rows of data are to be inserted at a time.
	private static final String INSERT_SQL_NODE =
		"INSERT INTO node(id, version, timestamp, user_id, visible, current, location)";
	private static final int INSERT_PRM_COUNT_NODE = 7;
	private static final String INSERT_SQL_NODE_TAG =
		"INSERT INTO node_tag(node_id, node_version, key, value)";
	private static final int INSERT_PRM_COUNT_NODE_TAG = 4;
	
	// These constants define how many rows of each data type will be inserted
	// with single insert statements.
	private static final int INSERT_BULK_ROW_COUNT_NODE = 100;
	private static final int INSERT_BULK_ROW_COUNT_NODE_TAG = 100;
	
	// These constants will be configured by a static code block.
	private static final String INSERT_SQL_SINGLE_NODE;
	private static final String INSERT_SQL_SINGLE_NODE_TAG;
	private static final String INSERT_SQL_BULK_NODE;
	private static final String INSERT_SQL_BULK_NODE_TAG;
	
	/**
	 * Defines the number of entities to write between each commit.
	 */
	private static final int COMMIT_ENTITY_COUNT = 100000;
	
	
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
		INSERT_SQL_SINGLE_NODE_TAG =
			buildSqlInsertStatement(INSERT_SQL_NODE_TAG, INSERT_PRM_COUNT_NODE_TAG, 1);
		INSERT_SQL_BULK_NODE =
			buildSqlInsertStatement(INSERT_SQL_NODE, INSERT_PRM_COUNT_NODE, INSERT_BULK_ROW_COUNT_NODE);
		INSERT_SQL_BULK_NODE_TAG =
			buildSqlInsertStatement(INSERT_SQL_NODE_TAG, INSERT_PRM_COUNT_NODE_TAG, INSERT_BULK_ROW_COUNT_NODE_TAG);
	}
	
	
	private DatabasePreferences preferences;
	private DatabaseContext dbCtx;
	private UserIdManager userIdManager;
	private SchemaVersionValidator schemaVersionValidator;
	private List<Node> nodeBuffer;
	private List<DBEntityTag> nodeTagBuffer;
	private long maxNodeId;
	private boolean initialized;
	private PreparedStatement singleNodeStatement;
	private PreparedStatement bulkNodeStatement;
	private PreparedStatement singleNodeTagStatement;
	private PreparedStatement bulkNodeTagStatement;
	private int uncommittedEntityCount;
	
	
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
	public PostgreSqlWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, boolean lockTables, boolean populateCurrentTables) {
		this.preferences = preferences;
		
		dbCtx = new DatabaseContext(loginCredentials);
		
		userIdManager = new UserIdManager(dbCtx);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
		
		nodeBuffer = new ArrayList<Node>();
		nodeTagBuffer = new ArrayList<DBEntityTag>();
		
		maxNodeId = 0;
		uncommittedEntityCount = 0;
		
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
			
			bulkNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE);
			singleNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE);
			bulkNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE_TAG);
			singleNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE_TAG);
			
			initialized = true;
		}
	}
	
	
	/**
	 * Commits outstanding changes to the database if a threshold of uncommitted
	 * data has been reached. This regular interval commit is intended to
	 * provide maximum performance.
	 */
	private void performIntervalCommit() {
		if (uncommittedEntityCount >= COMMIT_ENTITY_COUNT) {
			dbCtx.commit();
			
			uncommittedEntityCount = 0;
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
		
		try {
			statement.setLong(prmIndex++, node.getId());
			statement.setInt(prmIndex++, 1);
			statement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			statement.setLong(prmIndex++, userIdManager.getUserId());
			statement.setBoolean(prmIndex++, true);
			statement.setBoolean(prmIndex++, true);
			statement.setObject(prmIndex++, new PGpoint(node.getLongitude(), node.getLatitude()));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a node.", e);
		}
	}
	
	
	/**
	 * Sets tag values as bind variable parameters to a tag insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param entityTag
	 *            The entity tag containing the data to be inserted.
	 */
	private void populateEntityTagParameters(PreparedStatement statement, int initialIndex, DBEntityTag entityTag) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, entityTag.getEntityId());
			statement.setInt(prmIndex++, 1);
			statement.setString(prmIndex++, entityTag.getKey());
			statement.setString(prmIndex++, entityTag.getValue());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for an entity tag.", e);
		}
	}
	
	
	/**
	 * Flushes nodes to the database. If complete is false, this will only write
	 * nodes until the remaining way count is less than the multi-row insert
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
				
				populateNodeParameters(bulkNodeStatement, prmIndex, node);
				prmIndex += INSERT_PRM_COUNT_NODE;

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
				
				populateNodeParameters(singleNodeStatement, 1, node);
				
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
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		initialize();
		
		flushNodes(true);
		
		dbCtx.commit();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		dbCtx.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		entityContainer.process(this);
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
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		// Do nothing.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		// Do nothing.
	}
	
	
	/**
	 * Process the node tags.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	private void addNodeTags(Node node) {
		for (Tag tag : node.getTagList()) {
			nodeTagBuffer.add(new DBEntityTag(node.getId(), tag.getKey(), tag.getValue()));
		}
		
		flushNodeTags(false);
	}
}
