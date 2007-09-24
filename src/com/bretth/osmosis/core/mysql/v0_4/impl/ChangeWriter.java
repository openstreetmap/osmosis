package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.FixedPrecisionCoordinateConvertor;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.mysql.common.UserIdManager;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, timestamp, latitude, longitude, tile, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_CURRENT =
		"INSERT INTO current_nodes (id, timestamp, latitude, longitude, tile, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_NODE_CURRENT =
		"DELETE FROM current_nodes WHERE id = ?";
	private static final String INSERT_SQL_SEGMENT =
		"INSERT INTO segments (id, timestamp, node_a, node_b, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_SEGMENT_CURRENT =
		"INSERT INTO current_segments (id, timestamp, node_a, node_b, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_SEGMENT_CURRENT =
		"DELETE FROM current_segments WHERE id = ?";
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, version, timestamp, visible, user_id) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_CURRENT =
		"INSERT INTO current_ways (id, timestamp, visible, user_id) VALUES (?, ?, ?, ?)";
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
	
	private UserIdManager userIdManager;
	
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
	private FixedPrecisionCoordinateConvertor fixedPrecisionConvertor;
	private TileCalculator tileCalculator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public ChangeWriter(DatabaseLoginCredentials loginCredentials) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		userIdManager = new UserIdManager(dbCtx);
		
		tagFormatter = new EmbeddedTagProcessor();
		fixedPrecisionConvertor = new FixedPrecisionCoordinateConvertor();
		tileCalculator = new TileCalculator();
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
			
			// Get the result from the first row in the recordset if it exists.
			// If it doesn't exist, this is a create so we treat the existing
			// version as 0.
			if (resultSet.next()) {
				result = resultSet.getInt("version");
			} else {
				result = 0;
			}
			
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
		int prmIndex;
		
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
			prmIndex = 1;
			insertNodeStatement.setLong(prmIndex++, node.getId());
			insertNodeStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			insertNodeStatement.setInt(prmIndex++, fixedPrecisionConvertor.convertToFixed(node.getLatitude()));
			insertNodeStatement.setInt(prmIndex++, fixedPrecisionConvertor.convertToFixed(node.getLongitude()));
			insertNodeStatement.setInt(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
			insertNodeStatement.setString(prmIndex++, tagFormatter.format(node.getTagList()));
			insertNodeStatement.setBoolean(prmIndex++, visible);
			insertNodeStatement.setLong(prmIndex++, userIdManager.getUserId());
			
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
			prmIndex = 1;
			insertNodeCurrentStatement.setLong(prmIndex++, node.getId());
			insertNodeCurrentStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			insertNodeStatement.setInt(prmIndex++, fixedPrecisionConvertor.convertToFixed(node.getLatitude()));
			insertNodeStatement.setInt(prmIndex++, fixedPrecisionConvertor.convertToFixed(node.getLongitude()));
			insertNodeStatement.setInt(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
			insertNodeCurrentStatement.setString(prmIndex++, tagFormatter.format(node.getTagList()));
			insertNodeCurrentStatement.setBoolean(prmIndex++, visible);
			insertNodeCurrentStatement.setLong(prmIndex++, userIdManager.getUserId());
			
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
		int prmIndex;
		
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
			prmIndex = 1;
			insertSegmentStatement.setLong(prmIndex++, segment.getId());
			insertSegmentStatement.setTimestamp(prmIndex++, new Timestamp(segment.getTimestamp().getTime()));
			insertSegmentStatement.setLong(prmIndex++, segment.getFrom());
			insertSegmentStatement.setLong(prmIndex++, segment.getTo());
			insertSegmentStatement.setString(prmIndex++, tagFormatter.format(segment.getTagList()));
			insertSegmentStatement.setBoolean(prmIndex++, visible);
			insertSegmentStatement.setLong(prmIndex++, userIdManager.getUserId());
			
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
			prmIndex = 1;
			insertSegmentCurrentStatement.setLong(prmIndex++, segment.getId());
			insertSegmentCurrentStatement.setTimestamp(prmIndex++, new Timestamp(segment.getTimestamp().getTime()));
			insertSegmentCurrentStatement.setLong(prmIndex++, segment.getFrom());
			insertSegmentCurrentStatement.setLong(prmIndex++, segment.getTo());
			insertSegmentCurrentStatement.setString(prmIndex++, tagFormatter.format(segment.getTagList()));
			insertSegmentCurrentStatement.setBoolean(prmIndex++, visible);
			insertSegmentCurrentStatement.setLong(prmIndex++, userIdManager.getUserId());
			
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
		int prmIndex;
		int version;
		List<SegmentReference> segmentReferenceList;
		
		segmentReferenceList = way.getSegmentReferenceList();
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// Retrieve the existing way version. If it doesn't exist, we will
		// receive 0.
		version = getWayVersion(way.getId()) + 1;
		
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
			prmIndex = 1;
			insertWayStatement.setLong(prmIndex++, way.getId());
			insertWayStatement.setInt(prmIndex++, version);
			insertWayStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
			insertWayStatement.setBoolean(prmIndex++, visible);
			insertWayStatement.setLong(prmIndex++, userIdManager.getUserId());
			
			insertWayStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history way with id=" + way.getId() + ".", e);
		}
		
		// Insert the tags of the new way into the history table.
		for (Tag tag : way.getTagList()) {
			try {
				prmIndex = 1;
				insertWayTagStatement.setLong(prmIndex++, way.getId());
				insertWayTagStatement.setInt(prmIndex++, version);
				insertWayTagStatement.setString(prmIndex++, tag.getKey());
				insertWayTagStatement.setString(prmIndex++, tag.getValue());
				
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
				prmIndex = 1;
				insertWaySegmentStatement.setLong(prmIndex++, way.getId());
				insertWaySegmentStatement.setInt(prmIndex++, version);
				insertWaySegmentStatement.setLong(prmIndex++, segmentReference.getSegmentId());
				insertWaySegmentStatement.setLong(prmIndex++, i + 1);
				
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
			prmIndex = 1;
			insertWayCurrentStatement.setLong(prmIndex++, way.getId());
			insertWayCurrentStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
			insertWayCurrentStatement.setBoolean(prmIndex++, visible);
			insertWayCurrentStatement.setLong(prmIndex++, userIdManager.getUserId());
			
			insertWayCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current way with id=" + way.getId() + ".", e);
		}
		
		// Insert the tags of the new way into the current table.
		for (Tag tag : way.getTagList()) {
			try {
				prmIndex = 1;
				insertWayTagCurrentStatement.setLong(prmIndex++, way.getId());
				insertWayTagCurrentStatement.setString(prmIndex++, tag.getKey());
				insertWayTagCurrentStatement.setString(prmIndex++, tag.getValue());
				
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
				prmIndex = 1;
				insertWaySegmentCurrentStatement.setLong(prmIndex++, way.getId());
				insertWaySegmentCurrentStatement.setLong(prmIndex++, segmentReference.getSegmentId());
				insertWaySegmentCurrentStatement.setLong(prmIndex++, i);
				
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