package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Node;
import com.bretth.osmosis.data.Segment;
import com.bretth.osmosis.data.SegmentReference;
import com.bretth.osmosis.data.Tag;
import com.bretth.osmosis.data.Way;
import com.bretth.osmosis.task.ChangeAction;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, timestamp, latitude, longitude, tags, visible) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_CURRENT =
		"INSERT INTO current_nodes (id, timestamp, latitude, longitude, tags, visible) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_NODE_CURRENT =
		"DELETE FROM current_nodes WHERE id = ?";
	private static final String INSERT_SQL_SEGMENT =
		"INSERT INTO segments (id, node_a, node_b, tags, visible) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_SEGMENT_CURRENT =
		"INSERT INTO current_segments (id, node_a, node_b, tags, visible) VALUES (?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_SEGMENT_CURRENT =
		"DELETE FROM current_segments WHERE id = ?";
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, version, timestamp, visible) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_CURRENT =
		"INSERT INTO current_ways (id, timestamp, visible) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_CURRENT =
		"DELETE FROM current_ways WHERE id = ?";
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags (id, version, k, v) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_TAG_CURRENT =
		"INSERT INTO current_way_tags (id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_TAG_CURRENT =
		"DELETE FROM current_way_tags WHERE id = ?";
	private static final String INSERT_SQL_WAY_SEGMENT =
		"INSERT INTO way_segments (id, version, segment_id, sequence_id) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_SEGMENT_CURRENT =
		"INSERT INTO current_way_segments (id, segment_id, sequence_id) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_SEGMENT_CURRENT =
		"DELETE FROM current_way_segments WHERE id = ?";
	private static final String SELECT_SQL_WAY_CURRENT_VERSION =
		"SELECT MAX(version) AS version FROM ways WHERE id = ?";
	
	
	private DatabaseContext dbCtx;

	private PreparedStatement insertNodeStatement;
	private PreparedStatement insertNodeCurrentStatement;
	private PreparedStatement deleteNodeCurrentStatement;
	private PreparedStatement insertSegmentStatement;
	private PreparedStatement insertSegmentCurrentStatement;
	private PreparedStatement deleteSegmentCurrentStatement;
	private PreparedStatement insertWayStatement;
	private PreparedStatement insertWayCurrentStatement;
	private PreparedStatement deleteWayCurrentStatement;
	private PreparedStatement insertWayTagStatement;
	private PreparedStatement insertWayTagCurrentStatement;
	private PreparedStatement deleteWayTagCurrentStatement;
	private PreparedStatement insertWaySegmentStatement;
	private PreparedStatement insertWaySegmentCurrentStatement;
	private PreparedStatement deleteWaySegmentCurrentStatement;
	private PreparedStatement queryWayCurrentVersion;
	private EmbeddedTagProcessor tagFormatter;
	
	
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
	public ChangeWriter(String host, String database, String user, String password) {
		dbCtx = new DatabaseContext(host, database, user, password);
		
		tagFormatter = new EmbeddedTagProcessor();
	}
	
	
	/**
	 * Loads the current version of a way from the database.
	 * 
	 * @param wayId
	 *            The way to load.
	 * @return The existing version of the way.
	 */
	private int getWayVersion(long wayId) {
		ResultSet resultSet;
		int result;
		
		if (queryWayCurrentVersion == null) {
			queryWayCurrentVersion = dbCtx.prepareStatement(SELECT_SQL_WAY_CURRENT_VERSION);
		}
		
		try {
			// Query the current version of the specified way.
			queryWayCurrentVersion.setLong(1, wayId);
			resultSet = queryWayCurrentVersion.executeQuery();
			
			// Get the result from the first row in the recordset.
			resultSet.next();
			result = resultSet.getInt("version");
			
			resultSet.close();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("The version of way with id=" + wayId + " could not be loaded.", e);
		}
		
		return result;
	}
	
	
	/**
	 * Writes the specified node change to the database.
	 * 
	 * @param node
	 *            The node to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Node node, ChangeAction action) {
		boolean visible;
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// Create the prepared statements for node creation if necessary.
		if (insertNodeStatement == null) {
			insertNodeStatement = dbCtx.prepareStatement(INSERT_SQL_NODE);
			insertNodeCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_NODE_CURRENT);
			deleteNodeCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_NODE_CURRENT);
		}
		
		// Insert the new node into the history table.
		try {
			insertNodeStatement.setLong(1, node.getId());
			insertNodeStatement.setTimestamp(2, new Timestamp(node.getTimestamp().getTime()));
			insertNodeStatement.setDouble(3, node.getLatitude());
			insertNodeStatement.setDouble(4, node.getLongitude());
			insertNodeStatement.setString(5, tagFormatter.format(node.getTagList()));
			insertNodeStatement.setBoolean(6, visible);
			
			insertNodeStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history node with id=" + node.getId() + ".", e);
		}
		
		// Delete the existing node from the current table.
		try {
			deleteNodeCurrentStatement.setLong(1, node.getId());
			
			deleteNodeCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current node with id=" + node.getId() + ".", e);
		}
		
		// Insert the new node into the current table.
		try {
			insertNodeCurrentStatement.setLong(1, node.getId());
			insertNodeCurrentStatement.setTimestamp(2, new Timestamp(node.getTimestamp().getTime()));
			insertNodeCurrentStatement.setDouble(3, node.getLatitude());
			insertNodeCurrentStatement.setDouble(4, node.getLongitude());
			insertNodeCurrentStatement.setString(5, tagFormatter.format(node.getTagList()));
			insertNodeCurrentStatement.setBoolean(6, visible);
			
			insertNodeCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current node with id=" + node.getId() + ".", e);
		}
	}
	
	
	/**
	 * Writes the specified segment change to the database.
	 * 
	 * @param segment
	 *            The segment to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Segment segment, ChangeAction action) {
		boolean visible;
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// Create the prepared statements for segment creation if necessary.
		if (insertSegmentStatement == null) {
			insertSegmentStatement = dbCtx.prepareStatement(INSERT_SQL_SEGMENT);
			insertSegmentCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_SEGMENT_CURRENT);
			deleteSegmentCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_SEGMENT_CURRENT);
		}
		
		// Insert the new segment into the history table.
		try {
			insertSegmentStatement.setLong(1, segment.getId());
			insertSegmentStatement.setLong(2, segment.getFrom());
			insertSegmentStatement.setLong(3, segment.getTo());
			insertSegmentStatement.setString(4, tagFormatter.format(segment.getTagList()));
			insertSegmentStatement.setBoolean(5, visible);
			
			insertSegmentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history segment with id=" + segment.getId() + ".", e);
		}
		
		// Delete the existing segment from the current table.
		try {
			deleteSegmentCurrentStatement.setLong(1, segment.getId());
			
			deleteSegmentCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current segment with id=" + segment.getId() + ".", e);
		}
		
		// Insert the new node into the current table.
		try {
			insertSegmentCurrentStatement.setLong(1, segment.getId());
			insertSegmentCurrentStatement.setLong(2, segment.getFrom());
			insertSegmentCurrentStatement.setLong(3, segment.getTo());
			insertSegmentCurrentStatement.setString(4, tagFormatter.format(segment.getTagList()));
			insertSegmentCurrentStatement.setBoolean(5, visible);
			
			insertSegmentCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current segment with id=" + segment.getId() + ".", e);
		}
	}
	
	
	/**
	 * Writes the specified way change to the database.
	 * 
	 * @param way
	 *            The way to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Way way, ChangeAction action) {
		boolean visible;
		int version;
		List<SegmentReference> segmentReferenceList;
		
		segmentReferenceList = way.getSegmentReferenceList();
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// If this is a new record, the version is 1.
		// Else the version must be retrieved from the database.
		if (action.equals(ChangeAction.Create)) {
			version = 1;
		} else {
			version = getWayVersion(way.getId()) + 1;
		}
		
		// Create the prepared statements for way creation if necessary.
		if (insertWayStatement == null) {
			insertWayStatement = dbCtx.prepareStatement(INSERT_SQL_WAY);
			insertWayCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_CURRENT);
			deleteWayCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_CURRENT);
			insertWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_TAG);
			insertWayTagCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_TAG_CURRENT);
			deleteWayTagCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_TAG_CURRENT);
			insertWaySegmentStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_SEGMENT);
			insertWaySegmentCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_SEGMENT_CURRENT);
			deleteWaySegmentCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_SEGMENT_CURRENT);
		}
		
		// Insert the new way into the history table.
		try {
			insertWayStatement.setLong(1, way.getId());
			insertWayStatement.setInt(2, version);
			insertWayStatement.setTimestamp(3, new Timestamp(way.getTimestamp().getTime()));
			insertWayStatement.setBoolean(4, visible);
			
			insertWayStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history way with id=" + way.getId() + ".", e);
		}
		
		// Insert the tags of the new way into the history table.
		for (Tag tag : way.getTagList()) {
			try {
				insertWayTagStatement.setLong(1, way.getId());
				insertWayTagStatement.setInt(2, version);
				insertWayTagStatement.setString(3, tag.getKey());
				insertWayTagStatement.setString(4, tag.getValue());
				
				insertWayTagStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history way tag with id=" + way.getId()
					+ " and key=(" + tag.getKey() + ").", e);
			}
		}
		
		// Insert the segments of the new way into the history table.
		for (int i = 0; i < segmentReferenceList.size(); i++) {
			SegmentReference segmentReference;
			
			segmentReference = segmentReferenceList.get(i);
			
			try {
				insertWaySegmentStatement.setLong(1, way.getId());
				insertWaySegmentStatement.setInt(2, version);
				insertWaySegmentStatement.setLong(3, segmentReference.getSegmentId());
				insertWaySegmentStatement.setLong(4, i);
				
				insertWaySegmentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history way segment with way id=" + way.getId()
					+ " and segment id=" + segmentReference.getSegmentId() + ".", e);
			}
		}
		
		// Delete the existing way tags from the current table.
		try {
			deleteWayTagCurrentStatement.setLong(1, way.getId());
			
			deleteWayTagCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current way tags with id=" + way.getId() + ".", e);
		}
		// Delete the existing way segments from the current table.
		try {
			deleteWaySegmentCurrentStatement.setLong(1, way.getId());
			
			deleteWaySegmentCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current way segments with id=" + way.getId() + ".", e);
		}
		// Delete the existing way from the current table.
		try {
			deleteWayCurrentStatement.setLong(1, way.getId());
			
			deleteWayCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current way with id=" + way.getId() + ".", e);
		}
		
		// Insert the new way into the current table.
		try {
			insertWayCurrentStatement.setLong(1, way.getId());
			insertWayCurrentStatement.setTimestamp(2, new Timestamp(way.getTimestamp().getTime()));
			insertWayCurrentStatement.setBoolean(3, visible);
			
			insertWayCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current way with id=" + way.getId() + ".", e);
		}
		
		// Insert the tags of the new way into the current table.
		for (Tag tag : way.getTagList()) {
			try {
				insertWayTagCurrentStatement.setLong(1, way.getId());
				insertWayTagCurrentStatement.setString(2, tag.getKey());
				insertWayTagCurrentStatement.setString(3, tag.getValue());
				
				insertWayTagCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert current way tag with id=" + way.getId()
					+ " and key=(" + tag.getKey() + ").", e);
			}
		}
		
		// Insert the segments of the new way into the current table.
		for (int i = 0; i < segmentReferenceList.size(); i++) {
			SegmentReference segmentReference;
			
			segmentReference = segmentReferenceList.get(i);
			
			try {
				insertWaySegmentCurrentStatement.setLong(1, way.getId());
				insertWaySegmentCurrentStatement.setLong(2, segmentReference.getSegmentId());
				insertWaySegmentCurrentStatement.setLong(3, i);
				
				insertWaySegmentCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to insert current way segment with way id=" + way.getId()
						+ " and segment id=" + segmentReference.getSegmentId() + ".", e);
			}
		}
	}
	
	
	/**
	 * Flushes all changes to the database.
	 */
	public void complete() {
		dbCtx.commit();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		dbCtx.release();
	}
}