package com.bretth.osmosis.core.mysql.v0_5;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.UserIdManager;
import com.bretth.osmosis.core.mysql.v0_5.impl.EmbeddedTagProcessor;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBWayNode;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.task.v0_5.Sink;


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
		"INSERT INTO nodes(id, timestamp, latitude, longitude, tags, visible, user_id)";
	private static final int INSERT_PRM_COUNT_NODE = 7;
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, timestamp, version, visible, user_id)";
	private static final int INSERT_PRM_COUNT_WAY = 5;
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags (id, k, v, version)";
	private static final int INSERT_PRM_COUNT_WAY_TAG = 4;
	private static final String INSERT_SQL_WAY_NODE =
		"INSERT INTO way_nodes (id, node_id, sequence_id, version)";
	private static final int INSERT_PRM_COUNT_WAY_NODE = 4;
	
	// These SQL statements will be invoked prior to loading data to disable
	// indexes.
	private static final String[] INVOKE_DISABLE_KEYS = {
		"ALTER TABLE nodes DISABLE KEYS",
		"ALTER TABLE ways DISABLE KEYS",
		"ALTER TABLE way_tags DISABLE KEYS",
		"ALTER TABLE way_nodes DISABLE KEYS"
	};
	
	// These SQL statements will be invoked after loading data to re-enable
	// indexes.
	private static final String[] INVOKE_ENABLE_KEYS = {
		"ALTER TABLE nodes ENABLE KEYS",
		"ALTER TABLE ways ENABLE KEYS",
		"ALTER TABLE way_tags ENABLE KEYS",
		"ALTER TABLE way_nodes ENABLE KEYS"
	};
	
	// These SQL statements will be invoked after loading history tables to
	// populate the current tables.
	private static final int LOAD_CURRENT_NODE_ROW_COUNT = 1000000;
	private static final int LOAD_CURRENT_WAY_ROW_COUNT = 100000; // There are many node and tag records per way.
	private static final String LOAD_CURRENT_NODES =
		"INSERT INTO current_nodes SELECT * FROM nodes WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAYS =
		"INSERT INTO current_ways SELECT id, user_id, timestamp, visible FROM ways WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAY_TAGS =
		"INSERT INTO current_way_tags SELECT id, k, v FROM way_tags WHERE id >= ? AND id < ?";
	private static final String LOAD_CURRENT_WAY_NODES =
		"INSERT INTO current_way_nodes SELECT id, node_id, sequence_id FROM way_nodes WHERE id >= ? AND id < ?";
	
	// These SQL statements will be invoked to lock and unlock tables.
	private static final String INVOKE_LOCK_TABLES =
		"LOCK TABLES"
		+ " nodes WRITE, ways WRITE, way_tags WRITE, way_nodes WRITE,"
		+ " current_nodes WRITE, current_ways WRITE, current_way_tags WRITE, current_way_nodes WRITE,"
		+ " users WRITE";
	private static final String INVOKE_UNLOCK_TABLES = "UNLOCK TABLES";
	
	// These constants define how many rows of each data type will be inserted
	// with single insert statements.
	private static final int INSERT_BULK_ROW_COUNT_NODE = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_TAG = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_NODE = 100;
	
	// These constants will be configured by a static code block.
	private static final String INSERT_SQL_SINGLE_NODE;
	private static final String INSERT_SQL_BULK_NODE;
	private static final String INSERT_SQL_SINGLE_WAY;
	private static final String INSERT_SQL_BULK_WAY;
	private static final String INSERT_SQL_SINGLE_WAY_TAG;
	private static final String INSERT_SQL_BULK_WAY_TAG;
	private static final String INSERT_SQL_SINGLE_WAY_NODE;
	private static final String INSERT_SQL_BULK_WAY_NODE;
	
	
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
	}
	
	
	private DatabaseContext dbCtx;
	private UserIdManager userIdManager;
	private boolean lockTables;
	private boolean populateCurrentTables;
	private List<Node> nodeBuffer;
	private List<Way> wayBuffer;
	private List<DBEntityTag> wayTagBuffer;
	private List<DBWayNode> wayNodeBuffer;
	private long maxNodeId;
	private long maxWayId;
	private EmbeddedTagProcessor tagProcessor;
	private boolean initialized;
	private PreparedStatement singleNodeStatement;
	private PreparedStatement bulkNodeStatement;
	private PreparedStatement singleWayStatement;
	private PreparedStatement bulkWayStatement;
	private PreparedStatement singleWayTagStatement;
	private PreparedStatement bulkWayTagStatement;
	private PreparedStatement singleWayNodeStatement;
	private PreparedStatement bulkWayNodeStatement;
	private PreparedStatement loadCurrentNodesStatement;
	private PreparedStatement loadCurrentWaysStatement;
	private PreparedStatement loadCurrentWayTagsStatement;
	private PreparedStatement loadCurrentWayNodesStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param lockTables
	 *            If true, all tables will be locked during loading.
	 * @param populateCurrentTables
	 *            If true, the current tables will be populated as well as
	 *            history tables.
	 */
	public MysqlWriter(DatabaseLoginCredentials loginCredentials, boolean lockTables, boolean populateCurrentTables) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		userIdManager = new UserIdManager(dbCtx);
		
		this.lockTables = lockTables;
		this.populateCurrentTables = populateCurrentTables;
		
		nodeBuffer = new ArrayList<Node>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<DBEntityTag>();
		wayNodeBuffer = new ArrayList<DBWayNode>();
		
		maxNodeId = 0;
		maxWayId = 0;
		
		tagProcessor = new EmbeddedTagProcessor();
		
		initialized = false;
	}
	
	
	/**
	 * Initialises prepared statements and obtains database locks. Can be called
	 * multiple times.
	 */
	private void initialize() {
		if (!initialized) {
			bulkNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE);
			singleNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE);
			bulkWayStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY);
			singleWayStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY);
			bulkWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_TAG);
			singleWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_TAG);
			bulkWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_NODE);
			singleWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_NODE);
			
			loadCurrentNodesStatement = dbCtx.prepareStatement(LOAD_CURRENT_NODES);
			loadCurrentWaysStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAYS);
			loadCurrentWayTagsStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAY_TAGS);
			loadCurrentWayNodesStatement = dbCtx.prepareStatement(LOAD_CURRENT_WAY_NODES);
			
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
		
		try {
			statement.setLong(prmIndex++, node.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			statement.setDouble(prmIndex++, node.getLatitude());
			statement.setDouble(prmIndex++, node.getLongitude());
			statement.setString(prmIndex++, tagProcessor.format(node.getTagList()));
			statement.setBoolean(prmIndex++, true);
			statement.setLong(prmIndex++, userIdManager.getUserId());
			
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
		
		try {
			statement.setLong(prmIndex++, way.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
			statement.setInt(prmIndex++, 1);
			statement.setBoolean(prmIndex++, true);
			statement.setLong(prmIndex++, userIdManager.getUserId());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way.", e);
		}
	}
	
	
	/**
	 * Sets tag values as bind variable parameters to a way tag insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param wayTag
	 *            The way tag containing the data to be inserted.
	 */
	private void populateWayTagParameters(PreparedStatement statement, int initialIndex, DBEntityTag wayTag) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, wayTag.getEntityId());
			statement.setString(prmIndex++, wayTag.getKey());
			statement.setString(prmIndex++, wayTag.getValue());
			statement.setInt(prmIndex++, 1);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way tag.", e);
		}
	}
	
	
	/**
	 * Sets node reference values as bind variable parameters to a way node insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param wayNode
	 *            The way node containing the data to be inserted.
	 */
	private void populateWayNodeParameters(PreparedStatement statement, int initialIndex, DBWayNode wayNode) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, wayNode.getWayId());
			statement.setLong(prmIndex++, wayNode.getNodeId());
			statement.setInt(prmIndex++, wayNode.getSequenceId());
			statement.setInt(prmIndex++, 1);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way node.", e);
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
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_NODE; i++) {
				populateNodeParameters(bulkNodeStatement, prmIndex, nodeBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_NODE;
			}
			
			try {
				bulkNodeStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert nodes into the database.", e);
			}
		}
		
		if (complete) {
			while (nodeBuffer.size() > 0) {
				populateNodeParameters(singleNodeStatement, 1, nodeBuffer.remove(0));
				
				try {
					singleNodeStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a node into the database.", e);
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
				populateWayTagParameters(bulkWayTagStatement, prmIndex, wayTagBuffer.remove(0));
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
				populateWayTagParameters(singleWayTagStatement, 1, wayTagBuffer.remove(0));
				
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
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		initialize();
		
		flushNodes(true);
		flushWays(true);
		flushWayTags(true);
		flushWayNodes(true);
		
		// Re-enable indexes now that the load has completed.
		for (int i = 0; i < INVOKE_DISABLE_KEYS.length; i++) {
			dbCtx.executeStatement(INVOKE_ENABLE_KEYS[i]);
		}
		
		if (populateCurrentTables) {
			// Copy data into the current node tables.
			for (int i = 0; i < maxNodeId; i += LOAD_CURRENT_NODE_ROW_COUNT) {
				try {
					loadCurrentNodesStatement.setInt(1, i);
					loadCurrentNodesStatement.setInt(2, i + LOAD_CURRENT_NODE_ROW_COUNT);
					
					loadCurrentNodesStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to load current nodes.", e);
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
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relation) {
		// TODO: Auto-generated method stub	
	}
	
	
	/**
	 * Process the way tags.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWayTags(Way way) {
		for (Tag tag: way.getTagList()) {
			wayTagBuffer.add(new DBEntityTag(way.getId(), tag.getKey(), tag.getValue()));
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
			wayNodeBuffer.add(new DBWayNode(
				way.getId(),
				nodeReferenceList.get(i).getNodeId(),
				i + 1
			));
		}
		
		flushWayNodes(false);
	}
}
