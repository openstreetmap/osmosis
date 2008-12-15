// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_5;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.postgis.PGgeometry;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_5.Entity;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBRelationMember;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBWayNode;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.PointBuilder;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.pgsql.v0_5.impl.MemberTypeValueMapper;
import com.bretth.osmosis.core.task.v0_5.Sink;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlWriter implements Sink, EntityProcessor {
	
	private static final Logger log = Logger.getLogger(PostgreSqlWriter.class.getName());
	
	
	private static final String PRE_LOAD_SQL[] = {
		"ALTER TABLE nodes DROP CONSTRAINT pk_nodes",
		"ALTER TABLE ways DROP CONSTRAINT pk_ways",
		"ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes",
		"ALTER TABLE relations DROP CONSTRAINT pk_relations",
		"DROP INDEX idx_node_tags_node_id",
		"DROP INDEX idx_nodes_geom",
		"DROP INDEX idx_way_tags_way_id",
		"DROP INDEX idx_relation_tags_relation_id",
		"DROP INDEX idx_ways_bbox",
		"DROP INDEX idx_way_nodes_node_id"
	};
	
	private static final String POST_LOAD_SQL[] = {
		"ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id)",
		"ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id)",
		"ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id)",
		"ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id)",
		"CREATE INDEX idx_node_tags_node_id ON node_tags USING btree (node_id)",
		"CREATE INDEX idx_nodes_geom ON nodes USING gist (geom)",
		"CREATE INDEX idx_way_tags_way_id ON way_tags USING btree (way_id)",
		"CREATE INDEX idx_relation_tags_relation_id ON relation_tags USING btree (relation_id)",
		"UPDATE ways SET bbox = (SELECT Envelope(Collect(geom)) FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id WHERE way_nodes.way_id = ways.id)",
		"CREATE INDEX idx_ways_bbox ON ways USING gist (bbox)",
		"CREATE INDEX idx_way_nodes_node_id ON way_nodes USING btree (node_id)"
	};
	
	
	// These SQL strings are the prefix to statements that will be built based
	// on how many rows of data are to be inserted at a time.
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes(id, tstamp, user_id, user_name, geom)";
	private static final int INSERT_PRM_COUNT_NODE = 5;
	private static final String INSERT_SQL_NODE_TAG =
		"INSERT INTO node_tags(node_id, k, v)";
	private static final int INSERT_PRM_COUNT_NODE_TAG = 3;
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways(id, tstamp, user_id, user_name)";
	private static final int INSERT_PRM_COUNT_WAY = 4;
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags(way_id, k, v)";
	private static final int INSERT_PRM_COUNT_WAY_TAG = 3;
	private static final String INSERT_SQL_WAY_NODE =
		"INSERT INTO way_nodes(way_id, node_id, sequence_id)";
	private static final int INSERT_PRM_COUNT_WAY_NODE = 3;
	private static final String INSERT_SQL_RELATION =
		"INSERT INTO relations(id, tstamp, user_id, user_name)";
	private static final int INSERT_PRM_COUNT_RELATION = 4;
	private static final String INSERT_SQL_RELATION_TAG =
		"INSERT INTO relation_tags(relation_id, k, v)";
	private static final int INSERT_PRM_COUNT_RELATION_TAG = 3;
	private static final String INSERT_SQL_RELATION_MEMBER =
		"INSERT INTO relation_members(relation_id, member_id, member_type, member_role)";
	private static final int INSERT_PRM_COUNT_RELATION_MEMBER = 4;
	
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
	
	// These constants will be configured by a static code block.
	private static final String INSERT_SQL_SINGLE_NODE;
	private static final String INSERT_SQL_SINGLE_NODE_TAG;
	private static final String INSERT_SQL_SINGLE_WAY;
	private static final String INSERT_SQL_SINGLE_WAY_TAG;
	private static final String INSERT_SQL_SINGLE_WAY_NODE;
	private static final String INSERT_SQL_SINGLE_RELATION;
	private static final String INSERT_SQL_SINGLE_RELATION_TAG;
	private static final String INSERT_SQL_SINGLE_RELATION_MEMBER;
	private static final String INSERT_SQL_BULK_NODE;
	private static final String INSERT_SQL_BULK_NODE_TAG;
	private static final String INSERT_SQL_BULK_WAY;
	private static final String INSERT_SQL_BULK_WAY_TAG;
	private static final String INSERT_SQL_BULK_WAY_NODE;
	private static final String INSERT_SQL_BULK_RELATION;
	private static final String INSERT_SQL_BULK_RELATION_TAG;
	private static final String INSERT_SQL_BULK_RELATION_MEMBER;
	
	/**
	 * Defines the number of entities to write between each commit.
	 */
	private static final int COMMIT_ENTITY_COUNT = -1;
	
	
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
		INSERT_SQL_SINGLE_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, 1);
		INSERT_SQL_SINGLE_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, 1);
		INSERT_SQL_SINGLE_WAY_NODE =
			buildSqlInsertStatement(INSERT_SQL_WAY_NODE, INSERT_PRM_COUNT_WAY_NODE, 1);
		INSERT_SQL_SINGLE_RELATION =
			buildSqlInsertStatement(INSERT_SQL_RELATION, INSERT_PRM_COUNT_RELATION, 1);
		INSERT_SQL_SINGLE_RELATION_TAG =
			buildSqlInsertStatement(INSERT_SQL_RELATION_TAG, INSERT_PRM_COUNT_RELATION_TAG, 1);
		INSERT_SQL_SINGLE_RELATION_MEMBER =
			buildSqlInsertStatement(INSERT_SQL_RELATION_MEMBER, INSERT_PRM_COUNT_RELATION_MEMBER, 1);
		INSERT_SQL_BULK_NODE =
			buildSqlInsertStatement(INSERT_SQL_NODE, INSERT_PRM_COUNT_NODE, INSERT_BULK_ROW_COUNT_NODE);
		INSERT_SQL_BULK_NODE_TAG =
			buildSqlInsertStatement(INSERT_SQL_NODE_TAG, INSERT_PRM_COUNT_NODE_TAG, INSERT_BULK_ROW_COUNT_NODE_TAG);
		INSERT_SQL_BULK_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, INSERT_BULK_ROW_COUNT_WAY);
		INSERT_SQL_BULK_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, INSERT_BULK_ROW_COUNT_WAY_TAG);
		INSERT_SQL_BULK_WAY_NODE =
			buildSqlInsertStatement(INSERT_SQL_WAY_NODE, INSERT_PRM_COUNT_WAY_NODE, INSERT_BULK_ROW_COUNT_WAY_NODE);
		INSERT_SQL_BULK_RELATION =
			buildSqlInsertStatement(INSERT_SQL_RELATION, INSERT_PRM_COUNT_RELATION, INSERT_BULK_ROW_COUNT_RELATION);
		INSERT_SQL_BULK_RELATION_TAG =
			buildSqlInsertStatement(INSERT_SQL_RELATION_TAG, INSERT_PRM_COUNT_RELATION_TAG, INSERT_BULK_ROW_COUNT_RELATION_TAG);
		INSERT_SQL_BULK_RELATION_MEMBER =
			buildSqlInsertStatement(INSERT_SQL_RELATION_MEMBER, INSERT_PRM_COUNT_RELATION_MEMBER, INSERT_BULK_ROW_COUNT_RELATION_MEMBER);
	}
	
	
	private DatabaseContext dbCtx;
	private SchemaVersionValidator schemaVersionValidator;
	private List<Node> nodeBuffer;
	private List<DBEntityTag> nodeTagBuffer;
	private List<Way> wayBuffer;
	private List<DBEntityTag> wayTagBuffer;
	private List<DBWayNode> wayNodeBuffer;
	private List<Relation> relationBuffer;
	private List<DBEntityTag> relationTagBuffer;
	private List<DBRelationMember> relationMemberBuffer;
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
	private MemberTypeValueMapper memberTypeValueMapper;
	private int uncommittedEntityCount;
	private PointBuilder pointBuilder;
	
	
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
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
		
		nodeBuffer = new ArrayList<Node>();
		nodeTagBuffer = new ArrayList<DBEntityTag>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<DBEntityTag>();
		wayNodeBuffer = new ArrayList<DBWayNode>();
		relationBuffer = new ArrayList<Relation>();
		relationTagBuffer = new ArrayList<DBEntityTag>();
		relationMemberBuffer = new ArrayList<DBRelationMember>();
		
		memberTypeValueMapper = new MemberTypeValueMapper();
		pointBuilder = new PointBuilder();
		
		uncommittedEntityCount = 0;
		
		initialized = false;
	}
	
	
	/**
	 * Initialises prepared statements and obtains database locks. Can be called
	 * multiple times.
	 */
	private void initialize() {
		if (!initialized) {
			schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
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
			
			// Drop all constraints and indexes.
			log.fine("Running pre-load SQL statements.");
			for (int i = 0; i < PRE_LOAD_SQL.length; i++) {
				log.finer("SQL: " + PRE_LOAD_SQL[i]);
				dbCtx.executeStatement(PRE_LOAD_SQL[i]);
			}
			log.fine("Pre-load SQL statements complete.");
			log.fine("Loading data.");
			
			initialized = true;
		}
	}
	
	
	/**
	 * Commits outstanding changes to the database if a threshold of uncommitted
	 * data has been reached. This regular interval commit is intended to
	 * provide maximum performance.
	 */
	private void performIntervalCommit() {
		if (COMMIT_ENTITY_COUNT >= 0 && uncommittedEntityCount >= COMMIT_ENTITY_COUNT) {
			System.err.println("Commit");
			dbCtx.commit();
			
			uncommittedEntityCount = 0;
		}
	}
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	private int populateEntityParameters(PreparedStatement statement, int initialIndex, Entity entity) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		// We can't write an entity with a null timestamp.
		if (entity.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Entity(" + entity.getType() + ") " + entity.getId() + " does not have a timestamp set.");
		}
		
		try {
			// node(id, timestamp, user, location)
			statement.setLong(prmIndex++, entity.getId());
			statement.setTimestamp(prmIndex++, new Timestamp(entity.getTimestamp().getTime()));
			statement.setInt(prmIndex++, entity.getUser().getId());
			statement.setString(prmIndex++, entity.getUser().getName());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to set a prepared statement parameter for entity("
					+ entity.getType() + ") " + entity.getId() + ".", e);
		}
		
		return prmIndex;
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
	 * @return The current parameter offset.
	 */
	private int populateEntityTagParameters(PreparedStatement statement, int initialIndex, DBEntityTag dbEntityTag) {
		int prmIndex;
		Tag tag;
		
		prmIndex = initialIndex;
		
		tag = dbEntityTag.getTag();
		
		try {
			statement.setLong(prmIndex++, dbEntityTag.getEntityId());
			statement.setString(prmIndex++, tag.getKey());
			statement.setString(prmIndex++, tag.getValue());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for an entity tag.", e);
		}
		
		return prmIndex;
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
	 * @return The current parameter offset.
	 */
	private int populateNodeParameters(PreparedStatement statement, int initialIndex, Node node) {
		int prmIndex;
		
		// Populate the entity level parameters.
		prmIndex = populateEntityParameters(statement, initialIndex, node);
		
		try {
			// Set the node level parameters.
			statement.setObject(prmIndex++, new PGgeometry(pointBuilder.createPoint(node.getLatitude(), node.getLongitude())));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for node " + node.getId() + ".", e);
		}
		
		return prmIndex;
	}
	
	
	/**
	 * Sets way node values as bind variable parameters to a way node insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param dbWayNode
	 *            The database way node containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	private int populateWayNodeParameters(PreparedStatement statement, int initialIndex, DBWayNode dbWayNode) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			// statement parameters.
			statement.setLong(prmIndex++, dbWayNode.getWayId());
			statement.setLong(prmIndex++, dbWayNode.getWayNode().getNodeId());
			statement.setInt(prmIndex++, dbWayNode.getSequenceId());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for way node with wayId=" + dbWayNode.getWayId() + " and nodeId=" + dbWayNode.getWayNode().getNodeId() + ".", e);
		}
		
		return prmIndex;
	}
	
	
	/**
	 * Sets relation member values as bind variable parameters to a relation member insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param dbRelationMember
	 *            The database relation member containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	private int populateRelationMemberParameters(PreparedStatement statement, int initialIndex, DBRelationMember dbRelationMember) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			// statement parameters.
			statement.setLong(prmIndex++, dbRelationMember.getRelationId());
			statement.setLong(prmIndex++, dbRelationMember.getRelationMember().getMemberId());
			statement.setByte(prmIndex++, memberTypeValueMapper.getMemberType(dbRelationMember.getRelationMember().getMemberType()));
			statement.setString(prmIndex++, dbRelationMember.getRelationMember().getMemberRole());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for relation member with relationId=" + dbRelationMember.getRelationId() + " and memberId=" + dbRelationMember.getRelationMember().getMemberId() + ".", e);
		}
		
		return prmIndex;
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
	 * Process the node tags.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	private void addNodeTags(Node node) {
		for (Tag tag : node.getTagList()) {
			nodeTagBuffer.add(new DBEntityTag(node.getId(), tag));
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
				prmIndex = populateEntityTagParameters(bulkNodeTagStatement, prmIndex, nodeTagBuffer.remove(0));
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
				
				prmIndex = populateEntityParameters(bulkWayStatement, prmIndex, way);
				
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
				
				populateEntityParameters(singleWayStatement, 1, way);
				
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
			wayTagBuffer.add(new DBEntityTag(way.getId(), tag));
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
				prmIndex = populateEntityTagParameters(bulkWayTagStatement, prmIndex, wayTagBuffer.remove(0));
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
				prmIndex = populateWayNodeParameters(bulkWayNodeStatement, prmIndex, wayNodeBuffer.remove(0));
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
				
				prmIndex = populateEntityParameters(bulkRelationStatement, prmIndex, relation);
				
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
				
				populateEntityParameters(singleRelationStatement, 1, relation);
				
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
			relationTagBuffer.add(new DBEntityTag(relation.getId(), tag));
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
			relationMemberBuffer.add(new DBRelationMember(relation.getId(), relationMember));
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
				prmIndex = populateEntityTagParameters(bulkRelationTagStatement, prmIndex, relationTagBuffer.remove(0));
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
				prmIndex = populateRelationMemberParameters(bulkRelationMemberStatement, prmIndex, relationMemberBuffer.remove(0));
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
		log.fine("Post-load SQL complete.");
		
		dbCtx.commit();
		
		log.fine("Commit complete");
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
}
