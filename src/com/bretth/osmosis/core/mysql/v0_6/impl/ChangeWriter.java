// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, version, timestamp, visible, changeset_id, latitude, longitude, tile) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_CURRENT =
		"INSERT INTO current_nodes (id, version, timestamp, visible, changeset_id, latitude, longitude, tile) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SQL_NODE_CURRENT =
		"DELETE FROM current_nodes WHERE id = ?";
	private static final String INSERT_SQL_NODE_TAG =
		"INSERT INTO node_tags (id, version, k, v) VALUES (?, ?, ?, ?)";
	private static final String INSERT_SQL_NODE_TAG_CURRENT =
		"INSERT INTO current_node_tags (id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_NODE_TAG_CURRENT =
		"DELETE FROM current_node_tags WHERE id = ?";
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_WAY_CURRENT =
		"INSERT INTO current_ways (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";
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
	private static final String INSERT_SQL_RELATION =
		"INSERT INTO relations (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_SQL_RELATION_CURRENT =
		"INSERT INTO current_relations (id, version, timestamp, visible, changeset_id) VALUES (?, ?, ?, ?, ?)";
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
	
	
	private DatabaseContext dbCtx;
	
	private UserManager userManager;
	private ChangesetManager changesetManager;
	
	private boolean populateCurrentTables;
	private PreparedStatement insertNodeStatement;
	private PreparedStatement insertNodeCurrentStatement;
	private PreparedStatement deleteNodeCurrentStatement;
	private PreparedStatement insertNodeTagStatement;
	private PreparedStatement insertNodeTagCurrentStatement;
	private PreparedStatement deleteNodeTagCurrentStatement;
	private PreparedStatement insertWayStatement;
	private PreparedStatement insertWayCurrentStatement;
	private PreparedStatement deleteWayCurrentStatement;
	private PreparedStatement insertWayTagStatement;
	private PreparedStatement insertWayTagCurrentStatement;
	private PreparedStatement deleteWayTagCurrentStatement;
	private PreparedStatement insertWayNodeStatement;
	private PreparedStatement insertWayNodeCurrentStatement;
	private PreparedStatement deleteWayNodeCurrentStatement;
	private PreparedStatement insertRelationStatement;
	private PreparedStatement insertRelationCurrentStatement;
	private PreparedStatement deleteRelationCurrentStatement;
	private PreparedStatement insertRelationTagStatement;
	private PreparedStatement insertRelationTagCurrentStatement;
	private PreparedStatement deleteRelationTagCurrentStatement;
	private PreparedStatement insertRelationMemberStatement;
	private PreparedStatement insertRelationMemberCurrentStatement;
	private PreparedStatement deleteRelationMemberCurrentStatement;
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
		
		userManager = new UserManager(dbCtx);
		changesetManager = new ChangesetManager(dbCtx);
		
		this.populateCurrentTables = populateCurrentTables;
		
		tileCalculator = new TileCalculator();
		memberTypeRenderer = new MemberTypeRenderer();
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
		
		// Add or update the user in the database.
		userManager.addOrUpdateUser(node.getUser());
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
		// Create the prepared statements for node creation if necessary.
		if (insertNodeStatement == null) {
			insertNodeStatement = dbCtx.prepareStatement(INSERT_SQL_NODE);
			insertNodeCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_NODE_CURRENT);
			deleteNodeCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_NODE_CURRENT);
			insertNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_NODE_TAG);
			insertNodeTagCurrentStatement = dbCtx.prepareStatement(INSERT_SQL_NODE_TAG_CURRENT);
			deleteNodeTagCurrentStatement = dbCtx.prepareStatement(DELETE_SQL_NODE_TAG_CURRENT);
		}
		
		// Insert the new node into the history table.
		try {
			prmIndex = 1;
			insertNodeStatement.setLong(prmIndex++, node.getId());
			insertNodeStatement.setInt(prmIndex++, node.getVersion());
			insertNodeStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
			insertNodeStatement.setBoolean(prmIndex++, visible);
			insertNodeStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(node.getUser()));
			insertNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
			insertNodeStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
			insertNodeStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
			
			insertNodeStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history node with id=" + node.getId() + ".", e);
		}
		
		// Insert the tags of the new node into the history table.
		for (Tag tag : node.getTagList()) {
			try {
				prmIndex = 1;
				insertNodeTagStatement.setLong(prmIndex++, node.getId());
				insertNodeTagStatement.setInt(prmIndex++, node.getVersion());
				insertNodeTagStatement.setString(prmIndex++, tag.getKey());
				insertNodeTagStatement.setString(prmIndex++, tag.getValue());
				
				insertNodeTagStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert history node tag with id=" + node.getId()
					+ " and key=(" + tag.getKey() + ").", e);
			}
		}
		
		if (populateCurrentTables) {
			// Delete the existing node tags from the current table.
			try {
				deleteNodeTagCurrentStatement.setLong(1, node.getId());
				
				deleteNodeTagCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to delete current node tags with id=" + node.getId() + ".", e);
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
				insertNodeCurrentStatement.setInt(prmIndex++, node.getVersion());
				insertNodeCurrentStatement.setTimestamp(prmIndex++, new Timestamp(node.getTimestamp().getTime()));
				insertNodeCurrentStatement.setBoolean(prmIndex++, visible);
				insertNodeCurrentStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(node.getUser()));
				insertNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
				insertNodeCurrentStatement.setInt(prmIndex++, FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
				insertNodeCurrentStatement.setLong(prmIndex++, tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()));
				
				insertNodeCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert current node with id=" + node.getId() + ".", e);
			}
			
			// Insert the tags of the new node into the current table.
			for (Tag tag : node.getTagList()) {
				try {
					prmIndex = 1;
					insertNodeTagCurrentStatement.setLong(prmIndex++, node.getId());
					insertNodeTagCurrentStatement.setString(prmIndex++, tag.getKey());
					insertNodeTagCurrentStatement.setString(prmIndex++, tag.getValue());
					
					insertNodeTagCurrentStatement.execute();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException(
						"Unable to insert current node tag with id=" + node.getId()
						+ " and key=(" + tag.getKey() + ").", e);
				}
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
		List<WayNode> nodeReferenceList;
		
		// We can't write an entity with a null timestamp.
		if (way.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Way " + way.getId() + " does not have a timestamp set.");
		}
		
		// Add or update the user in the database.
		userManager.addOrUpdateUser(way.getUser());
		
		nodeReferenceList = way.getWayNodeList();
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
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
			insertWayStatement.setInt(prmIndex++, way.getVersion());
			insertWayStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
			insertWayStatement.setBoolean(prmIndex++, visible);
			insertWayStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(way.getUser()));
			
			insertWayStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history way with id=" + way.getId() + ".", e);
		}
		
		// Insert the tags of the new way into the history table.
		for (Tag tag : way.getTagList()) {
			try {
				prmIndex = 1;
				insertWayTagStatement.setLong(prmIndex++, way.getId());
				insertWayTagStatement.setInt(prmIndex++, way.getVersion());
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
				insertWayNodeStatement.setInt(prmIndex++, way.getVersion());
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
				insertWayCurrentStatement.setInt(prmIndex++, way.getVersion());
				insertWayCurrentStatement.setTimestamp(prmIndex++, new Timestamp(way.getTimestamp().getTime()));
				insertWayCurrentStatement.setBoolean(prmIndex++, visible);
				insertWayCurrentStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(way.getUser()));
				
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
		List<RelationMember> relationMemberList;
		
		// We can't write an entity with a null timestamp.
		if (relation.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Relation " + relation.getId() + " does not have a timestamp set.");
		}
		
		// Add or update the user in the database.
		userManager.addOrUpdateUser(relation.getUser());
		
		relationMemberList = relation.getMemberList();
		
		// If this is a deletion, the entity is not visible.
		visible = !action.equals(ChangeAction.Delete);
		
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
			insertRelationStatement.setInt(prmIndex++, relation.getVersion());
			insertRelationStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
			insertRelationStatement.setBoolean(prmIndex++, visible);
			insertRelationStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(relation.getUser()));
			
			insertRelationStatement.execute();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert history relation with id=" + relation.getId() + ".", e);
		}
		
		// Insert the tags of the new relation into the history table.
		for (Tag tag : relation.getTagList()) {
			try {
				prmIndex = 1;
				insertRelationTagStatement.setLong(prmIndex++, relation.getId());
				insertRelationTagStatement.setInt(prmIndex++, relation.getVersion());
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
				insertRelationMemberStatement.setLong(prmIndex++, relation.getId());
				insertRelationMemberStatement.setInt(prmIndex++, relation.getVersion());
				insertRelationMemberStatement.setString(prmIndex++, memberTypeRenderer.render(relationMember.getMemberType()));
				insertRelationMemberStatement.setLong(prmIndex++, relationMember.getMemberId());
				insertRelationMemberStatement.setString(prmIndex++, relationMember.getMemberRole());
				
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
				prmIndex = 1;
				insertRelationCurrentStatement.setLong(prmIndex++, relation.getId());
				insertRelationCurrentStatement.setInt(prmIndex++, relation.getVersion());
				insertRelationCurrentStatement.setTimestamp(prmIndex++, new Timestamp(relation.getTimestamp().getTime()));
				insertRelationCurrentStatement.setBoolean(prmIndex++, visible);
				insertRelationCurrentStatement.setLong(prmIndex++, changesetManager.obtainChangesetId(relation.getUser()));
				
				insertRelationCurrentStatement.execute();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert current relation with id=" + relation.getId() + ".", e);
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
					insertRelationMemberCurrentStatement.setLong(prmIndex++, relation.getId());
					insertRelationMemberCurrentStatement.setString(prmIndex++, memberTypeRenderer.render(relationMember.getMemberType()));
					insertRelationMemberCurrentStatement.setLong(prmIndex++, relationMember.getMemberId());
					insertRelationMemberCurrentStatement.setString(prmIndex++, relationMember.getMemberRole());
					
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
		userManager.release();
		changesetManager.release();
		
		dbCtx.release();
	}
}