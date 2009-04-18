// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_5.WayNode;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;
import org.openstreetmap.osmosis.core.util.TileCalculator;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, timestamp, latitude, longitude, tile, tags, visible, user_id)"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_CURRENT =
		"INSERT INTO current_nodes (id, timestamp, latitude, longitude, tile, tags, visible, user_id)"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
	
	private boolean populateCurrentTables;
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
	private TileCalculator tileCalculator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param populateCurrentTables
	 *            If true, the current tables will be populated as well as
	 *            history tables.
	 */
	public ChangeWriter(DatabaseLoginCredentials loginCredentials, boolean populateCurrentTables) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		userIdManager = new UserIdManager(dbCtx);
		
		this.populateCurrentTables = populateCurrentTables;
		
		tagFormatter = new EmbeddedTagProcessor();
		tileCalculator = new TileCalculator();
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
			throw new OsmosisRuntimeException(
					"The version of relation with id=" + relationId + " could not be loaded.", e);
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

		// We can't write an entity with a null timestamp.
		if (node.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Node " + node.getId() + " does not have a timestamp set.");
		}
		
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
			insertNodeStatement.setLong(
					prmIndex++, node.getId());
			insertNodeStatement.setTimestamp(
					prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			insertNodeStatement.setInt(
					prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
			insertNodeStatement.setInt(
					prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
			insertNodeStatement.setLong(
					prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
			insertNodeStatement.setString(
					prmIndex++, tagFormatter.format(node.getTagList()));
			insertNodeStatement.setBoolean(
					prmIndex++, visible);
			insertNodeStatement.setLong(
					prmIndex++, userIdManager.getUserId());
			
			insertNodeStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history node with id=" + node.getId() + ".", e);
		}
		
		if (populateCurrentTables) {
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
				insertNodeCurrentStatement.setLong(
						prmIndex++, node.getId());
				insertNodeCurrentStatement.setTimestamp(
						prmIndex++, new Timestamp(node.getTimestamp().getTime()));
				insertNodeCurrentStatement.setInt(
						prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
				insertNodeCurrentStatement.setInt(
						prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
				insertNodeCurrentStatement.setLong(
						prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
				insertNodeCurrentStatement.setString(
						prmIndex++, tagFormatter.format(node.getTagList()));
				insertNodeCurrentStatement.setBoolean(
						prmIndex++, visible);
				insertNodeCurrentStatement.setLong(
						prmIndex++, userIdManager.getUserId());
				
				insertNodeCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert current node with id=" + node.getId() + ".", e);
			}
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
		List<WayNode> nodeReferenceList;

		// We can't write an entity with a null timestamp.
		if (way.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Way " + way.getId() + " does not have a timestamp set.");
		}
		
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
		
		// Insert the nodes of the new way into the history table.
		for (int i = 0; i < nodeReferenceList.size(); i++) {
			WayNode nodeReference;
			
			nodeReference = nodeReferenceList.get(i);
			
			try {
				prmIndex = 1;
				insertWayNodeStatement.setLong(prmIndex++, way.getId());
				insertWayNodeStatement.setInt(prmIndex++, version);
				insertWayNodeStatement.setLong(prmIndex++, nodeReference.getNodeId());
				insertWayNodeStatement.setLong(prmIndex++, i + 1);
				
				insertWayNodeStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history way node with way id=" + way.getId()
					+ " and node id=" + nodeReference.getNodeId() + ".", e);
			}
		}
		
		if (populateCurrentTables) {
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
			
			// Insert the nodes of the new way into the current table.
			for (int i = 0; i < nodeReferenceList.size(); i++) {
				WayNode nodeReference;
				
				nodeReference = nodeReferenceList.get(i);
				
				try {
					prmIndex = 1;
					insertWayNodeCurrentStatement.setLong(prmIndex++, way.getId());
					insertWayNodeCurrentStatement.setLong(prmIndex++, nodeReference.getNodeId());
					insertWayNodeCurrentStatement.setLong(prmIndex++, i);
					
					insertWayNodeCurrentStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException(
							"Unable to insert current way node with way id=" + way.getId()
							+ " and node id=" + nodeReference.getNodeId() + ".", e);
				}
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
		int prmIndex;
		int version;
		List<RelationMember> relationMemberList;

		// We can't write an entity with a null timestamp.
		if (relation.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Relation " + relation.getId() + " does not have a timestamp set.");
		}
		
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
			prmIndex = 1;
			insertRelationStatement.setLong(prmIndex++, relation.getId());
			insertRelationStatement.setInt(prmIndex++, version);
			insertRelationStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
			insertRelationStatement.setBoolean(prmIndex++, visible);
			insertRelationStatement.setLong(prmIndex++, userIdManager.getUserId());
			
			insertRelationStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history relation with id=" + relation.getId() + ".", e);
		}
		
		// Insert the tags of the new relation into the history table.
		for (Tag tag : relation.getTagList()) {
			try {
				prmIndex = 1;
				insertRelationTagStatement.setLong(prmIndex++, relation.getId());
				insertRelationTagStatement.setInt(prmIndex++, version);
				insertRelationTagStatement.setString(prmIndex++, tag.getKey());
				insertRelationTagStatement.setString(prmIndex++, tag.getValue());
				
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
				prmIndex = 1;
				insertRelationMemberStatement.setLong(
						prmIndex++, relation.getId());
				insertRelationMemberStatement.setInt(
						prmIndex++, version);
				insertRelationMemberStatement.setString(
						prmIndex++, memberTypeRenderer.render(relationMember.getMemberType()));
				insertRelationMemberStatement.setLong(
						prmIndex++, relationMember.getMemberId());
				insertRelationMemberStatement.setString(
						prmIndex++, relationMember.getMemberRole());
				
				insertRelationMemberStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history relation member with relation id=" + relation.getId()
					+ ", member type=" + relationMember.getMemberId()
					+ " and member id=" + relationMember.getMemberId() + ".", e);
			}
		}
		
		if (populateCurrentTables) {
			// Delete the existing relation tags from the current table.
			try {
				deleteRelationTagCurrentStatement.setLong(1, relation.getId());
				
				deleteRelationTagCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to delete current relation tags with id=" + relation.getId() + ".", e);
			}
			// Delete the existing relation members from the current table.
			try {
				deleteRelationMemberCurrentStatement.setLong(1, relation.getId());
				
				deleteRelationMemberCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to delete current relation members with id=" + relation.getId() + ".", e);
			}
			// Delete the existing relation from the current table.
			try {
				deleteRelationCurrentStatement.setLong(1, relation.getId());
				
				deleteRelationCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to delete current relation with id=" + relation.getId() + ".", e);
			}
			
			// Insert the new relation into the current table.
			try {
				prmIndex = 1;
				insertRelationCurrentStatement.setLong(
						prmIndex++, relation.getId());
				insertRelationCurrentStatement.setTimestamp(
						prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
				insertRelationCurrentStatement.setBoolean(
						prmIndex++, visible);
				insertRelationCurrentStatement.setLong(
						prmIndex++, userIdManager.getUserId());
				
				insertRelationCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Unable to insert current relation with id=" + relation.getId() + ".", e);
			}
			
			// Insert the tags of the new relation into the current table.
			for (Tag tag : relation.getTagList()) {
				try {
					prmIndex = 1;
					insertRelationTagCurrentStatement.setLong(prmIndex++, relation.getId());
					insertRelationTagCurrentStatement.setString(prmIndex++, tag.getKey());
					insertRelationTagCurrentStatement.setString(prmIndex++, tag.getValue());
					
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
					prmIndex = 1;
					insertRelationMemberCurrentStatement.setLong(
							prmIndex++, relation.getId());
					insertRelationMemberCurrentStatement.setString(
							prmIndex++, memberTypeRenderer.render(relationMember.getMemberType()));
					insertRelationMemberCurrentStatement.setLong(
							prmIndex++, relationMember.getMemberId());
					insertRelationMemberCurrentStatement.setString(
							prmIndex++, relationMember.getMemberRole());
					
					insertRelationMemberCurrentStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException(
							"Unable to insert current relation member with relation id=" + relation.getId()
							+ ", member type=" + relationMember.getMemberId()
							+ " and member id=" + relationMember.getMemberId() + ".", e);
				}
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
		userIdManager.release();
		
		dbCtx.release();
	}
}
