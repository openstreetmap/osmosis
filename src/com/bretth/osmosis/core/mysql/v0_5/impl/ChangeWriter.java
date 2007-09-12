package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.NodeReference;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.UserIdManager;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, timestamp, latitude, longitude, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_CURRENT =
		"INSERT INTO current_nodes (id, timestamp, latitude, longitude, tags, visible, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_NODE_CURRENT =
		"DELETE FROM current_nodes WHERE id = ?";
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
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public ChangeWriter(DatabaseLoginCredentials loginCredentials) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		userIdManager = new UserIdManager(dbCtx);
		
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
			insertNodeStatement.setLong(7, userIdManager.getUserId());
			
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
			insertNodeCurrentStatement.setLong(7, userIdManager.getUserId());
			
			insertNodeCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current node with id=" + node.getId() + ".", e);
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
		List<NodeReference> nodeReferenceList;
		
		nodeReferenceList = way.getNodeReferenceList();
		
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
			insertWayStatement.setLong(1, way.getId());
			insertWayStatement.setInt(2, version);
			insertWayStatement.setTimestamp(3, new Timestamp(way.getTimestamp().getTime()));
			insertWayStatement.setBoolean(4, visible);
			insertWayStatement.setLong(5, userIdManager.getUserId());
			
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
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			NodeReference nodeReference;
			
			nodeReference = nodeReferenceList.get(i);
			
			try {
				insertWaySegmentStatement.setLong(1, way.getId());
				insertWaySegmentStatement.setInt(2, version);
				insertWaySegmentStatement.setLong(3, nodeReference.getNodeId());
				insertWaySegmentStatement.setLong(4, i + 1);
				
				insertWaySegmentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history way segment with way id=" + way.getId()
					+ " and segment id=" + nodeReference.getNodeId() + ".", e);
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
			insertWayCurrentStatement.setLong(4, userIdManager.getUserId());
			
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
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			NodeReference nodeReference;
			
			nodeReference = nodeReferenceList.get(i);
			
			try {
				insertWaySegmentCurrentStatement.setLong(1, way.getId());
				insertWaySegmentCurrentStatement.setLong(2, nodeReference.getNodeId());
				insertWaySegmentCurrentStatement.setLong(3, i);
				
				insertWaySegmentCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to insert current way segment with way id=" + way.getId()
						+ " and segment id=" + nodeReference.getNodeId() + ".", e);
			}
		}
	}
	
	
	/**
	 * Writes the specified relation change to the database.
	 * 
	 * @param relation
	 *            The relation to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Relation relation, ChangeAction action) {
		// TODO: Complete this method.
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