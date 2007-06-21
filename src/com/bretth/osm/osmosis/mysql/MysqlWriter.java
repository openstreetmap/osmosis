package com.bretth.osm.osmosis.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.SegmentReference;
import com.bretth.osm.osmosis.data.Tag;
import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.mysql.impl.DatabaseContext;
import com.bretth.osm.osmosis.mysql.impl.WaySegment;
import com.bretth.osm.osmosis.mysql.impl.WayTag;
import com.bretth.osm.osmosis.task.Sink;


/**
 * An OSM data sink for storing all data to a database. This task is intended
 * for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class MysqlWriter implements Sink {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes(id, latitude, longitude, tags)";
	private static final int INSERT_PRM_COUNT_NODE = 4;
	private static final String INSERT_SQL_SEGMENT =
		"INSERT INTO segments (id, node_a, node_b, tags)";
	private static final int INSERT_PRM_COUNT_SEGMENT = 4;
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id)";
	private static final int INSERT_PRM_COUNT_WAY = 1;
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags (id, k, v)";
	private static final int INSERT_PRM_COUNT_WAY_TAG = 3;
	private static final String INSERT_SQL_WAY_SEGMENT =
		"INSERT INTO way_segments (id, segment_id, sequence_id)";
	private static final int INSERT_PRM_COUNT_WAY_SEGMENT = 3;
	
	private static final int INSERT_BULK_ROW_COUNT_NODE = 100;
	private static final int INSERT_BULK_ROW_COUNT_SEGMENT = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_TAG = 100;
	private static final int INSERT_BULK_ROW_COUNT_WAY_SEGMENT = 100;
	
	private static final String INSERT_SQL_SINGLE_NODE;
	private static final String INSERT_SQL_BULK_NODE;
	private static final String INSERT_SQL_SINGLE_SEGMENT;
	private static final String INSERT_SQL_BULK_SEGMENT;
	private static final String INSERT_SQL_SINGLE_WAY;
	private static final String INSERT_SQL_BULK_WAY;
	private static final String INSERT_SQL_SINGLE_WAY_TAG;
	private static final String INSERT_SQL_BULK_WAY_TAG;
	private static final String INSERT_SQL_SINGLE_WAY_SEGMENT;
	private static final String INSERT_SQL_BULK_WAY_SEGMENT;
	
	
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
		INSERT_SQL_SINGLE_SEGMENT =
			buildSqlInsertStatement(INSERT_SQL_SEGMENT, INSERT_PRM_COUNT_SEGMENT, 1);
		INSERT_SQL_BULK_SEGMENT =
			buildSqlInsertStatement(INSERT_SQL_SEGMENT, INSERT_PRM_COUNT_SEGMENT, INSERT_BULK_ROW_COUNT_SEGMENT);
		INSERT_SQL_SINGLE_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, 1);
		INSERT_SQL_BULK_WAY =
			buildSqlInsertStatement(INSERT_SQL_WAY, INSERT_PRM_COUNT_WAY, INSERT_BULK_ROW_COUNT_WAY);
		INSERT_SQL_SINGLE_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, 1);
		INSERT_SQL_BULK_WAY_TAG =
			buildSqlInsertStatement(INSERT_SQL_WAY_TAG, INSERT_PRM_COUNT_WAY_TAG, INSERT_BULK_ROW_COUNT_WAY_TAG);
		INSERT_SQL_SINGLE_WAY_SEGMENT =
			buildSqlInsertStatement(INSERT_SQL_WAY_SEGMENT, INSERT_PRM_COUNT_WAY_SEGMENT, 1);
		INSERT_SQL_BULK_WAY_SEGMENT =
			buildSqlInsertStatement(INSERT_SQL_WAY_SEGMENT, INSERT_PRM_COUNT_WAY_SEGMENT, INSERT_BULK_ROW_COUNT_WAY_SEGMENT);
	}
	
	
	private DatabaseContext dbCtx;
	private List<Node> nodeBuffer;
	private List<Segment> segmentBuffer;
	private List<Way> wayBuffer;
	private List<WayTag> wayTagBuffer;
	private List<WaySegment> waySegmentBuffer;
	private PreparedStatement singleNodeStatement;
	private PreparedStatement bulkNodeStatement;
	private PreparedStatement singleSegmentStatement;
	private PreparedStatement bulkSegmentStatement;
	private PreparedStatement singleWayStatement;
	private PreparedStatement bulkWayStatement;
	private PreparedStatement singleWayTagStatement;
	private PreparedStatement bulkWayTagStatement;
	private PreparedStatement singleWaySegmentStatement;
	private PreparedStatement bulkWaySegmentStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public MysqlWriter(String host, String database, String user, String password) {
		dbCtx = new DatabaseContext(host, database, user, password);

		nodeBuffer = new ArrayList<Node>();
		segmentBuffer = new ArrayList<Segment>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<WayTag>();
		waySegmentBuffer = new ArrayList<WaySegment>();
	}
	
	
	/**
	 * Removes references to all statements. They aren't closed because this is
	 * handled by the underlying database context.
	 */
	private void clearStatements() {
		singleNodeStatement = null;
		bulkNodeStatement = null;
		singleSegmentStatement = null;
		bulkSegmentStatement = null;
		singleWayStatement = null;
		bulkWayStatement = null;
		singleWayTagStatement = null;
		bulkWayTagStatement = null;
		singleWaySegmentStatement = null;
		bulkWaySegmentStatement = null;
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
		StringBuilder tagBuffer;
		int prmIndex;
		
		tagBuffer = new StringBuilder();
		for (Tag tag : node.getTagList()) {
			tagBuffer.append(";").append(tag.getKey()).append("=").append(tag.getValue());
		}
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, node.getId());
			statement.setDouble(prmIndex++, node.getLatitude());
			statement.setDouble(prmIndex++, node.getLongitude());
			statement.setString(prmIndex++, tagBuffer.toString());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a node.", e);
		}
	}
	
	
	/**
	 * Sets segment values as bind variable parameters to a segment insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param segment
	 *            The segment containing the data to be inserted.
	 */
	private void populateSegmentParameters(PreparedStatement statement, int initialIndex, Segment segment) {
		StringBuilder tagBuffer;
		int prmIndex;
		
		tagBuffer = new StringBuilder();
		for (Tag tag : segment.getTagList()) {
			tagBuffer.append(";").append(tag.getKey()).append("=").append(tag.getValue());
		}
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, segment.getId());
			statement.setDouble(prmIndex++, segment.getFrom());
			statement.setDouble(prmIndex++, segment.getTo());
			statement.setString(prmIndex++, tagBuffer.toString());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a segment.", e);
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
	private void populateWayTagParameters(PreparedStatement statement, int initialIndex, WayTag wayTag) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, wayTag.getWayId());
			statement.setString(prmIndex++, wayTag.getKey());
			statement.setString(prmIndex++, wayTag.getValue());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way tag.", e);
		}
	}
	
	
	/**
	 * Sets segment reference values as bind variable parameters to a way segment insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param initialIndex
	 *            The offset index of the first variable to set.
	 * @param waySegment
	 *            The way segment containing the data to be inserted.
	 */
	private void populateWaySegmentParameters(PreparedStatement statement, int initialIndex, WaySegment waySegment) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, waySegment.getWayId());
			statement.setLong(prmIndex++, waySegment.getSegmentId());
			statement.setInt(prmIndex++, waySegment.getSequenceId());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to set a prepared statement parameter for a way segment.", e);
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
			
			if (bulkNodeStatement == null) {
				clearStatements();
				bulkNodeStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_NODE);
			}
			
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
				if (singleNodeStatement == null) {
					clearStatements();
					singleNodeStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_NODE);
				}
				
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
	 * Flushes segments to the database. If complete is false, this will only
	 * write segments until the remaining segment count is less than the
	 * multi-row insert statement row count. If complete is true, all remaining
	 * rows will be written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushSegments(boolean complete) {
		while (segmentBuffer.size() >= INSERT_BULK_ROW_COUNT_SEGMENT) {
			int prmIndex;
			
			if (bulkSegmentStatement == null) {
				clearStatements();
				bulkSegmentStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_SEGMENT);
			}
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_SEGMENT; i++) {
				populateSegmentParameters(bulkSegmentStatement, prmIndex, segmentBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_SEGMENT;
			}
			
			try {
				bulkSegmentStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert segments into the database.", e);
			}
		}
		
		if (complete) {
			while (segmentBuffer.size() > 0) {
				if (singleSegmentStatement == null) {
					clearStatements();
					singleSegmentStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_SEGMENT);
				}
				
				populateSegmentParameters(singleSegmentStatement, 1, segmentBuffer.remove(0));
				
				try {
					singleSegmentStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a segment into the database.", e);
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
			
			if (bulkWayStatement == null) {
				clearStatements();
				bulkWayStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY);
			}
			
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
				addWaySegments(way);
			}
		}
		
		if (complete) {
			while (wayBuffer.size() > 0) {
				Way way;
				
				way = wayBuffer.remove(0);
				
				if (singleWayStatement == null) {
					clearStatements();
					singleWayStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY);
				}
				
				populateWayParameters(singleWayStatement, 1, way);
				
				try {
					singleWayStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way into the database.", e);
				}
				
				addWayTags(way);
				addWaySegments(way);
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
			
			if (bulkWayTagStatement == null) {
				clearStatements();
				bulkWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_TAG);
			}
			
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
				if (singleWayTagStatement == null) {
					clearStatements();
					singleWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_TAG);
				}
				
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
	 * Flushes way segments to the database. If complete is false, this will only write
	 * way segments until the remaining way segment count is less than the multi-row insert
	 * statement row count. If complete is true, all remaining rows will be
	 * written using single row insert statements.
	 * 
	 * @param complete
	 *            If true, all data will be written to the database. If false,
	 *            some data may be left until more data is available.
	 */
	private void flushWaySegments(boolean complete) {
		while (waySegmentBuffer.size() >= INSERT_BULK_ROW_COUNT_WAY_SEGMENT) {
			int prmIndex;
			
			if (bulkWaySegmentStatement == null) {
				clearStatements();
				bulkWaySegmentStatement = dbCtx.prepareStatement(INSERT_SQL_BULK_WAY_SEGMENT);
			}
			
			prmIndex = 1;
			for (int i = 0; i < INSERT_BULK_ROW_COUNT_WAY_SEGMENT; i++) {
				populateWaySegmentParameters(bulkWaySegmentStatement, prmIndex, waySegmentBuffer.remove(0));
				prmIndex += INSERT_PRM_COUNT_WAY_SEGMENT;
			}
			
			try {
				bulkWaySegmentStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to bulk insert way segments into the database.", e);
			}
		}
		
		if (complete) {
			while (waySegmentBuffer.size() > 0) {
				if (singleWaySegmentStatement == null) {
					clearStatements();
					singleWaySegmentStatement = dbCtx.prepareStatement(INSERT_SQL_SINGLE_WAY_SEGMENT);
				}
				
				populateWaySegmentParameters(singleWaySegmentStatement, 1, waySegmentBuffer.remove(0));
				
				try {
					singleWaySegmentStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a way segment into the database.", e);
				}
			}
		}
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		flushNodes(true);
		flushSegments(true);
		flushWays(true);
		flushWayTags(true);
		flushWaySegments(true);
		
		dbCtx.commit();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		clearStatements();
		
		dbCtx.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processNode(Node node) {
		nodeBuffer.add(node);
		
		flushNodes(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processSegment(Segment segment) {
		flushNodes(true);
		
		segmentBuffer.add(segment);
		
		flushSegments(false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void processWay(Way way) {
		flushSegments(true);
		
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
			wayTagBuffer.add(new WayTag(way.getId(), tag.getKey(), tag.getValue()));
		}
		
		flushWayTags(false);
	}
	
	
	/**
	 * Process the way segments.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	private void addWaySegments(Way way) {
		List<SegmentReference> segmentReferenceList;
		
		segmentReferenceList = way.getSegmentReferenceList();
		
		for (int i = 0; i < segmentReferenceList.size(); i++) {
			waySegmentBuffer.add(new WaySegment(
				way.getId(),
				segmentReferenceList.get(i).getSegmentId(),
				i + 1
			));
		}
		
		flushWaySegments(false);
	}
}
