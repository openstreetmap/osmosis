package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
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
	private static final String INSERT_SQL_WAY_NODE =
		"INSERT INTO way_nodes (id, version, node_id, sequence_id) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_NODE_CURRENT =
		"INSERT INTO current_way_nodes (id, node_id, sequence_id) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_NODE_CURRENT =
		"DELETE FROM current_way_nodes WHERE id = ?";
	private static final String SELECT_SQL_WAY_CURRENT_VERSION =
		"SELECT MAX(version) AS version FROM ways WHERE id = ?";
	private static final String INSERT_SQL_RELATION =
		"INSERT INTO relations (id, version, timestamp, visible, user_id) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_RELATION_CURRENT =
		"INSERT INTO current_relations (id, timestamp, visible, user_id) VALUES (?, ?, ?, ?)";
	private static final String DELETE_SQL_RELATION_CURRENT =
		"DELETE FROM current_relations WHERE id = ?";
	private static final String INSERT_SQL_RELATION_TAG =
		"INSERT INTO relation_tags (id, version, k, v) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_RELATION_TAG_CURRENT =
		"INSERT INTO current_relation_tags (id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_RELATION_TAG_CURRENT =
		"DELETE FROM current_relation_tags WHERE id = ?";
	private static final String INSERT_SQL_RELATION_MEMBER =
		"INSERT INTO relation_members (id, version, member_type, member_id, member_role) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_RELATION_MEMBER_CURRENT =
		"INSERT INTO current_relation_members (id, member_type, member_id, member_role) VALUES (?, ?, ?, ?)";
	private static final String DELETE_SQL_RELATION_MEMBER_CURRENT =
		"DELETE FROM current_relation_members WHERE id = ?";
	private static final String SELECT_SQL_RELATION_CURRENT_VERSION =
		"SELECT MAX(version) AS version FROM relations WHERE id = ?";
	
	
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
	private PreparedStatement insertWayNodeStatement;
	private PreparedStatement insertWayNodeCurrentStatement;
	private PreparedStatement deleteWayNodeCurrentStatement;
	private PreparedStatement queryWayCurrentVersion;
	private PreparedStatement insertRelationStatement;
	private PreparedStatement insertRelationCurrentStatement;
	private PreparedStatement deleteRelationCurrentStatement;
	private PreparedStatement insertRelationTagStatement;
	private PreparedStatement insertRelationTagCurrentStatement;
	private PreparedStatement deleteRelationTagCurrentStatement;
	private PreparedStatement insertRelationMemberStatement;
	private PreparedStatement insertRelationMemberCurrentStatement;
	private PreparedStatement deleteRelationMemberCurrentStatement;
	private PreparedStatement queryRelationCurrentVersion;
	private EmbeddedTagProcessor tagFormatter;
	private MemberTypeRenderer memberTypeRenderer;
	
	
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
		memberTypeRenderer = new MemberTypeRenderer();
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
	 * Loads the current version of a relation from the database.
	 * 
	 * @param relationId
	 *            The relation to load.
	 * @return The existing version of the relation.
	 */
	private int getRelationVersion(long relationId) {
		ResultSet resultSet;
		int result;
		
		if (queryRelationCurrentVersion == null) {
			queryRelationCurrentVersion = dbCtx.prepareStatement(SELECT_SQL_RELATION_CURRENT_VERSION);
		}
		
		try {
			// Query the current version of the specified relation.
			queryRelationCurrentVersion.setLong(1, relationId);
			resultSet = queryRelationCurrentVersion.executeQuery();
			
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
			throw new OsmosisRuntimeException("The version of relation with id=" + relationId + " could not be loaded.", e);
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
		List<WayNode> nodeReferenceList;
		
		nodeReferenceList = way.getWayNodeList();
		
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
			insertWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_NODE);
			insertWayNodeCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_NODE_CURRENT);
			deleteWayNodeCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_NODE_CURRENT);
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
		
		// Insert the nodes of the new way into the history table.
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			WayNode nodeReference;
			
			nodeReference = nodeReferenceList.get(i);
			
			try {
				insertWayNodeStatement.setLong(1, way.getId());
				insertWayNodeStatement.setInt(2, version);
				insertWayNodeStatement.setLong(3, nodeReference.getNodeId());
				insertWayNodeStatement.setLong(4, i + 1);
				
				insertWayNodeStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history way node with way id=" + way.getId()
					+ " and node id=" + nodeReference.getNodeId() + ".", e);
			}
		}
		
		// Delete the existing way tags from the current table.
		try {
			deleteWayTagCurrentStatement.setLong(1, way.getId());
			
			deleteWayTagCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current way tags with id=" + way.getId() + ".", e);
		}
		// Delete the existing way nodes from the current table.
		try {
			deleteWayNodeCurrentStatement.setLong(1, way.getId());
			
			deleteWayNodeCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current way nodes with id=" + way.getId() + ".", e);
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
		
		// Insert the nodes of the new way into the current table.
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			WayNode nodeReference;
			
			nodeReference = nodeReferenceList.get(i);
			
			try {
				insertWayNodeCurrentStatement.setLong(1, way.getId());
				insertWayNodeCurrentStatement.setLong(2, nodeReference.getNodeId());
				insertWayNodeCurrentStatement.setLong(3, i);
				
				insertWayNodeCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to insert current way node with way id=" + way.getId()
						+ " and node id=" + nodeReference.getNodeId() + ".", e);
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
		boolean visible;
		int version;
		List<RelationMember> relationMemberList;
		
		relationMemberList = relation.getMemberList();
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// Retrieve the existing relation version. If it doesn't exist, we will
		// receive 0.
		version = getRelationVersion(relation.getId()) + 1;
		
		// Create the prepared statements for relation creation if necessary.
		if (insertRelationStatement == null) {
			insertRelationStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION);
			insertRelationCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_CURRENT);
			deleteRelationCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION_CURRENT);
			insertRelationTagStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_TAG);
			insertRelationTagCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_TAG_CURRENT);
			deleteRelationTagCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION_TAG_CURRENT);
			insertRelationMemberStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER);
			insertRelationMemberCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER_CURRENT);
			deleteRelationMemberCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION_MEMBER_CURRENT);
		}
		
		// Insert the new relation into the history table.
		try {
			insertRelationStatement.setLong(1, relation.getId());
			insertRelationStatement.setInt(2, version);
			insertRelationStatement.setTimestamp(3, new Timestamp(relation.getTimestamp().getTime()));
			insertRelationStatement.setBoolean(4, visible);
			insertRelationStatement.setLong(5, userIdManager.getUserId());
			
			insertRelationStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history relation with id=" + relation.getId() + ".", e);
		}
		
		// Insert the tags of the new relation into the history table.
		for (Tag tag : relation.getTagList()) {
			try {
				insertRelationTagStatement.setLong(1, relation.getId());
				insertRelationTagStatement.setInt(2, version);
				insertRelationTagStatement.setString(3, tag.getKey());
				insertRelationTagStatement.setString(4, tag.getValue());
				
				insertRelationTagStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history relation tag with id=" + relation.getId()
					+ " and key=(" + tag.getKey() + ").", e);
			}
		}
		
		// Insert the members of the new relation into the history table.
		for (int i = 0; i < relationMemberList.size(); i++) {
			RelationMember relationMember;
			
			relationMember = relationMemberList.get(i);
			
			try {
				insertRelationMemberStatement.setLong(1, relation.getId());
				insertRelationMemberStatement.setInt(2, version);
				insertRelationMemberStatement.setString(3, memberTypeRenderer.render(relationMember.getMemberType()));
				insertRelationMemberStatement.setLong(4, relationMember.getMemberId());
				insertRelationMemberStatement.setString(5, relationMember.getMemberRole());
				
				insertRelationMemberStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history relation member with relation id=" + relation.getId()
					+ ", member type=" + relationMember.getMemberId()
					+ " and member id=" + relationMember.getMemberId() + ".", e);
			}
		}
		
		// Delete the existing relation tags from the current table.
		try {
			deleteRelationTagCurrentStatement.setLong(1, relation.getId());
			
			deleteRelationTagCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current relation tags with id=" + relation.getId() + ".", e);
		}
		// Delete the existing relation members from the current table.
		try {
			deleteRelationMemberCurrentStatement.setLong(1, relation.getId());
			
			deleteRelationMemberCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current relation members with id=" + relation.getId() + ".", e);
		}
		// Delete the existing relation from the current table.
		try {
			deleteRelationCurrentStatement.setLong(1, relation.getId());
			
			deleteRelationCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete current relation with id=" + relation.getId() + ".", e);
		}
		
		// Insert the new relation into the current table.
		try {
			insertRelationCurrentStatement.setLong(1, relation.getId());
			insertRelationCurrentStatement.setTimestamp(2, new Timestamp(relation.getTimestamp().getTime()));
			insertRelationCurrentStatement.setBoolean(3, visible);
			insertRelationCurrentStatement.setLong(4, userIdManager.getUserId());
			
			insertRelationCurrentStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert current relation with id=" + relation.getId() + ".", e);
		}
		
		// Insert the tags of the new relation into the current table.
		for (Tag tag : relation.getTagList()) {
			try {
				insertRelationTagCurrentStatement.setLong(1, relation.getId());
				insertRelationTagCurrentStatement.setString(2, tag.getKey());
				insertRelationTagCurrentStatement.setString(3, tag.getValue());
				
				insertRelationTagCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert current relation tag with id=" + relation.getId()
					+ " and key=(" + tag.getKey() + ").", e);
			}
		}
		
		// Insert the members of the new relation into the current table.
		for (int i = 0; i < relationMemberList.size(); i++) {
			RelationMember relationMember;
			
			relationMember = relationMemberList.get(i);
			
			try {
				insertRelationMemberCurrentStatement.setLong(1, relation.getId());
				insertRelationMemberCurrentStatement.setString(2, memberTypeRenderer.render(relationMember.getMemberType()));
				insertRelationMemberCurrentStatement.setLong(3, relationMember.getMemberId());
				insertRelationMemberCurrentStatement.setString(4, relationMember.getMemberRole());
				
				insertRelationMemberCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to insert current relation member with relation id=" + relation.getId()
						+ ", member type=" + relationMember.getMemberId()
						+ " and member id=" + relationMember.getMemberId() + ".", e);
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