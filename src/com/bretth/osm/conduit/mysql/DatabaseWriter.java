package com.bretth.osm.conduit.mysql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.mysql.impl.DatabaseContext;
import com.bretth.osm.conduit.mysql.impl.WaySegment;
import com.bretth.osm.conduit.mysql.impl.WayTag;
import com.bretth.osm.conduit.task.OsmSink;


public class DatabaseWriter implements OsmSink {
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
	
	
	public DatabaseWriter() {
		dbCtx = new DatabaseContext();

		nodeBuffer = new ArrayList<Node>();
		segmentBuffer = new ArrayList<Segment>();
		wayBuffer = new ArrayList<Way>();
		wayTagBuffer = new ArrayList<WayTag>();
		waySegmentBuffer = new ArrayList<WaySegment>();
	}
	
	
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
			throw new ConduitRuntimeException("Unable to set a prepared statement parameter for a node.", e);
		}
	}
	
	
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
			throw new ConduitRuntimeException("Unable to set a prepared statement parameter for a segment.", e);
		}
	}
	
	
	private void populateWayParameters(PreparedStatement statement, int initialIndex, Way way) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, way.getId());
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to set a prepared statement parameter for a way.", e);
		}
	}
	
	
	private void populateWayTagParameters(PreparedStatement statement, int initialIndex, WayTag wayTag) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, wayTag.getWayId());
			statement.setString(prmIndex++, wayTag.getKey());
			statement.setString(prmIndex++, wayTag.getValue());
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to set a prepared statement parameter for a way tag.", e);
		}
	}
	
	
	private void populateWaySegmentParameters(PreparedStatement statement, int initialIndex, WaySegment waySegment) {
		int prmIndex;
		
		prmIndex = initialIndex;
		
		try {
			statement.setLong(prmIndex++, waySegment.getWayId());
			statement.setLong(prmIndex++, waySegment.getSegmentId());
			statement.setInt(prmIndex++, waySegment.getSequenceId());
			
		} catch (SQLException e) {
			throw new ConduitRuntimeException("Unable to set a prepared statement parameter for a way segment.", e);
		}
	}
	
	
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
				throw new ConduitRuntimeException("Unable to bulk insert nodes into the database.", e);
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
					throw new ConduitRuntimeException("Unable to insert a node into the database.", e);
				}
			}
		}
	}
	
	
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
				throw new ConduitRuntimeException("Unable to bulk insert segments into the database.", e);
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
					throw new ConduitRuntimeException("Unable to insert a segment into the database.", e);
				}
			}
		}
	}
	
	
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
				throw new ConduitRuntimeException("Unable to bulk insert ways into the database.", e);
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
					throw new ConduitRuntimeException("Unable to insert a way into the database.", e);
				}

				addWayTags(way);
				addWaySegments(way);
			}
		}
	}
	
	
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
				throw new ConduitRuntimeException("Unable to bulk insert way tags into the database.", e);
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
					throw new ConduitRuntimeException("Unable to insert a way tag into the database.", e);
				}
			}
		}
	}
	
	
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
				throw new ConduitRuntimeException("Unable to bulk insert way segments into the database.", e);
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
					throw new ConduitRuntimeException("Unable to insert a way segment into the database.", e);
				}
			}
		}
	}
	
	
	public void complete() {
		flushNodes(true);
		flushSegments(true);
		flushWays(true);
		flushWayTags(true);
		flushWaySegments(true);
		
		dbCtx.commit();
	}
	
	
	public void release() {
		clearStatements();
		
		dbCtx.release();
	}
	
	
	public void addNode(Node node) {
		nodeBuffer.add(node);
		
		flushNodes(false);
	}
	
	
	public void addSegment(Segment segment) {
		flushNodes(true);
		
		segmentBuffer.add(segment);
		
		flushSegments(false);
	}
	
	
	public void addWay(Way way) {
		flushSegments(true);
		
		wayBuffer.add(way);
		
		flushWays(false);
	}
	
	
	private void addWayTags(Way way) {
		for (Tag tag: way.getTagList()) {
			wayTagBuffer.add(new WayTag(way.getId(), tag));
		}
		
		flushWayTags(false);
	}
	
	
	private void addWaySegments(Way way) {
		List<SegmentReference> segmentReferenceList;
		
		segmentReferenceList = way.getSegmentReferenceList();
		
		for (int i = 0; i < segmentReferenceList.size(); i++) {
			waySegmentBuffer.add(new WaySegment(way.getId(), segmentReferenceList.get(i), i + 1));
		}
		
		flushWaySegments(false);
	}
}
