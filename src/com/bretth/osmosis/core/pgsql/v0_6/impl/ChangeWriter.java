// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.postgis.PGgeometry;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.PointBuilder;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	private static final String INSERT_SQL_NODE =
		"INSERT INTO nodes (id, user_name, tstamp, geom) VALUES (?, ?, ?, ?)";
	private static final String DELETE_SQL_NODE =
		"DELETE FROM nodes WHERE id = ?";
	private static final String INSERT_SQL_NODE_TAG =
		"INSERT INTO node_tags (node_id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_NODE_TAG =
		"DELETE FROM node_tags WHERE node_id = ?";
	private static final String UPDATE_NODE_WAY_BBOX =
		"UPDATE ways w SET bbox = (" +
		" SELECT Envelope(Collect(n.geom))" +
		" FROM nodes n INNER JOIN way_nodes wn ON wn.node_id = n.id" +
		" WHERE wn.way_id = w.id" +
		" )" +
		" WHERE w.id IN (" +
		" SELECT w.id FROM ways w INNER JOIN way_nodes wn ON w.id = wn.way_id WHERE wn.node_id = ? GROUP BY w.id" +
		" )";
	private static final String INSERT_SQL_WAY =
		"INSERT INTO ways (id, user_name, tstamp) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY =
		"DELETE FROM ways WHERE id = ?";
	private static final String INSERT_SQL_WAY_TAG =
		"INSERT INTO way_tags (way_id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_TAG =
		"DELETE FROM way_tags WHERE way_id = ?";
	private static final String INSERT_SQL_WAY_NODE =
		"INSERT INTO way_nodes (way_id, node_id, sequence_id) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_WAY_NODE =
		"DELETE FROM way_nodes WHERE way_id = ?";
	private static final String UPDATE_WAY_BBOX =
		"UPDATE ways SET bbox = (" +
		" SELECT Envelope(Collect(geom))" +
		" FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id" +
		" WHERE way_nodes.way_id = ways.id" +
		" )" +
		" WHERE ways.id = ?";
	private static final String INSERT_SQL_RELATION =
		"INSERT INTO relations (id, user_name, tstamp) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_RELATION =
		"DELETE FROM relations WHERE id = ?";
	private static final String INSERT_SQL_RELATION_TAG =
		"INSERT INTO relation_tags (relation_id, k, v) VALUES (?, ?, ?)";
	private static final String DELETE_SQL_RELATION_TAG =
		"DELETE FROM relation_tags WHERE relation_id = ?";
	private static final String INSERT_SQL_RELATION_MEMBER =
		"INSERT INTO relation_members (relation_id, member_id, member_type, member_role) VALUES (?, ?, ?, ?)";
	private static final String DELETE_SQL_RELATION_MEMBER =
		"DELETE FROM relation_members WHERE relation_id = ?";
	
	
	private DatabaseContext dbCtx;
	
	private PreparedStatement insertNodeStatement;
	private PreparedStatement deleteNodeStatement;
	private PreparedStatement insertNodeTagStatement;
	private PreparedStatement deleteNodeTagStatement;
	private PreparedStatement updateNodeWayPreparedStatement;
	private PreparedStatement insertWayStatement;
	private PreparedStatement deleteWayStatement;
	private PreparedStatement insertWayTagStatement;
	private PreparedStatement deleteWayTagStatement;
	private PreparedStatement insertWayNodeStatement;
	private PreparedStatement deleteWayNodeStatement;
	private PreparedStatement updateWayBboxStatement;
	private PreparedStatement insertRelationStatement;
	private PreparedStatement deleteRelationStatement;
	private PreparedStatement insertRelationTagStatement;
	private PreparedStatement deleteRelationTagStatement;
	private PreparedStatement insertRelationMemberStatement;
	private PreparedStatement deleteRelationMemberStatement;
	private MemberTypeValueMapper memberTypeValueMapper;
	private PointBuilder pointBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public ChangeWriter(DatabaseLoginCredentials loginCredentials) {
		dbCtx = new DatabaseContext(loginCredentials);
		
		memberTypeValueMapper = new MemberTypeValueMapper();
		pointBuilder = new PointBuilder();
	}
	
	
	/**
	 * Sets entity values as bind variable parameters to an entity insert query.
	 * 
	 * @param statement
	 *            The prepared statement to add the values to.
	 * @param entity
	 *            The entity containing the data to be inserted.
	 * @return The current parameter offset.
	 */
	private int populateEntityParameters(PreparedStatement statement, Entity entity) {
		int prmIndex;
		
		// We can't write an entity with a null timestamp.
		if (entity.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Entity(" + entity.getType() + ") " + entity.getId() + " does not have a timestamp set.");
		}
		
		try {
			prmIndex = 1;
			
			statement.setLong(prmIndex++, entity.getId());
			statement.setString(prmIndex++, entity.getUser());
			statement.setTimestamp(prmIndex++, new Timestamp(entity.getTimestamp().getTime()));
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to set a prepared statement parameter for entity("
					+ entity.getType() + ") " + entity.getId() + ".", e);
		}
		
		return prmIndex;
	}
	
	
	/**
	 * Writes all tags for the specified entity to the database.
	 * 
	 * @param statement
	 *            The prepared statement used to perform the inserts.
	 * @param entity
	 *            The entity containing the tags.
	 */
	private void writeEntityTags(PreparedStatement statement, Entity entity) {
		int prmIndex;
		
		for (Tag tag : entity.getTagList()) {
			try {
				prmIndex = 1;
				
				statement.setLong(prmIndex++, entity.getId());
				statement.setString(prmIndex++, tag.getKey());
				statement.setString(prmIndex++, tag.getValue());
				
				statement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert a new entity tag.", e);
			}
		}
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
		int prmIndex;
		
		// We can't write an entity with a null timestamp.
		if (node.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Node " + node.getId() + " does not have a timestamp set.");
		}
		
		// Create the prepared statements for node creation if necessary.
		if (insertNodeStatement == null) {
			insertNodeStatement = dbCtx.prepareStatement(INSERT_SQL_NODE);
			deleteNodeStatement = dbCtx.prepareStatement(DELETE_SQL_NODE);
			insertNodeTagStatement = dbCtx.prepareStatement(INSERT_SQL_NODE_TAG);
			deleteNodeTagStatement = dbCtx.prepareStatement(DELETE_SQL_NODE_TAG);
			updateNodeWayPreparedStatement = dbCtx.prepareStatement(UPDATE_NODE_WAY_BBOX);
		}
		
		// Delete any existing records for the node.
		prmIndex = 1;
		try {
			deleteNodeTagStatement.setLong(prmIndex++, node.getId());
			deleteNodeTagStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete tags for node " + node.getId() + ".", e);
		}
		prmIndex = 1;
		try {
			deleteNodeStatement.setLong(prmIndex++, node.getId());
			deleteNodeStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete node " + node.getId() + ".", e);
		}
		
		// If this is a create or modify, insert the new node records.
		if (ChangeAction.Create.equals(action) || ChangeAction.Modify.equals(action)) {
			prmIndex = populateEntityParameters(insertNodeStatement, node);
			try {
				insertNodeStatement.setObject(prmIndex++, new PGgeometry(pointBuilder.createPoint(node.getLatitude(), node.getLongitude())));
				insertNodeStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert node " + node.getId() + ".", e);
			}
			
			writeEntityTags(insertNodeTagStatement, node);

			prmIndex = 1;
			try {
				updateNodeWayPreparedStatement.setLong(prmIndex++, node.getId());
				updateNodeWayPreparedStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to update way bboxes related to node " + node.getId() + ".", e);
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
		int prmIndex;
		List<WayNode> wayNodeList;
		
		// We can't write an entity with a null timestamp.
		if (way.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Way " + way.getId() + " does not have a timestamp set.");
		}
		
		// Create the prepared statements for way creation if necessary.
		if (insertWayStatement == null) {
			insertWayStatement = dbCtx.prepareStatement(INSERT_SQL_WAY);
			deleteWayStatement = dbCtx.prepareStatement(DELETE_SQL_WAY);
			insertWayTagStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_TAG);
			deleteWayTagStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_TAG);
			insertWayNodeStatement = dbCtx.prepareStatement(INSERT_SQL_WAY_NODE);
			deleteWayNodeStatement = dbCtx.prepareStatement(DELETE_SQL_WAY_NODE);
			updateWayBboxStatement = dbCtx.prepareStatement(UPDATE_WAY_BBOX);
		}
		
		// Delete any existing records for the way.
		prmIndex = 1;
		try {
			deleteWayTagStatement.setLong(prmIndex++, way.getId());
			deleteWayTagStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete tags for way " + way.getId() + ".", e);
		}
		prmIndex = 1;
		try {
			deleteWayNodeStatement.setLong(prmIndex++, way.getId());
			deleteWayNodeStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete way nodes for way " + way.getId() + ".", e);
		}
		prmIndex = 1;
		try {
			deleteWayStatement.setLong(prmIndex++, way.getId());
			deleteWayStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete way " + way.getId() + ".", e);
		}
		
		// If this is a create or modify, insert the new way records.
		if (ChangeAction.Create.equals(action) || ChangeAction.Modify.equals(action)) {
			// Ignore ways with a single node because they can't be loaded into postgis.
			if (way.getWayNodeList().size() > 1) {
				prmIndex = populateEntityParameters(insertWayStatement, way);
				try {
					insertWayStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert way " + way.getId() + ".", e);
				}
				
				writeEntityTags(insertWayTagStatement, way);
				
				wayNodeList = way.getWayNodeList();
				for (int i = 0; i < wayNodeList.size(); i++) {
					WayNode wayNode;
					
					wayNode = wayNodeList.get(i);
					try {
						prmIndex = 1;
						
						insertWayNodeStatement.setLong(prmIndex++, way.getId());
						insertWayNodeStatement.setLong(prmIndex++, wayNode.getNodeId());
						insertWayNodeStatement.setInt(prmIndex++, i);
						
						insertWayNodeStatement.executeUpdate();
						
					} catch (SQLException e) {
						throw new OsmosisRuntimeException("Unable to insert a new way node.", e);
					}
				}
				
				prmIndex = 1;
				try {
					updateWayBboxStatement.setLong(prmIndex++, way.getId());
					updateWayBboxStatement.executeUpdate();
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to update bbox for way " + way.getId() + ".", e);
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
		int prmIndex;
		List<RelationMember> memberList;
		
		// We can't write an entity with a null timestamp.
		if (relation.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Way " + relation.getId() + " does not have a timestamp set.");
		}
		
		// Create the prepared statements for way creation if necessary.
		if (insertRelationStatement == null) {
			insertRelationStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION);
			deleteRelationStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION);
			insertRelationTagStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_TAG);
			deleteRelationTagStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION_TAG);
			insertRelationMemberStatement = dbCtx.prepareStatement(INSERT_SQL_RELATION_MEMBER);
			deleteRelationMemberStatement = dbCtx.prepareStatement(DELETE_SQL_RELATION_MEMBER);
		}
		
		// Delete any existing records for the relation.
		prmIndex = 1;
		try {
			deleteRelationTagStatement.setLong(prmIndex++, relation.getId());
			deleteRelationTagStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete tags for relation " + relation.getId() + ".", e);
		}
		prmIndex = 1;
		try {
			deleteRelationMemberStatement.setLong(prmIndex++, relation.getId());
			deleteRelationMemberStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete relation members for relation " + relation.getId() + ".", e);
		}
		prmIndex = 1;
		try {
			deleteRelationStatement.setLong(prmIndex++, relation.getId());
			deleteRelationStatement.executeUpdate();
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to delete relation " + relation.getId() + ".", e);
		}
		
		// If this is a create or modify, insert the new relation records.
		if (ChangeAction.Create.equals(action) || ChangeAction.Modify.equals(action)) {
			prmIndex = populateEntityParameters(insertRelationStatement, relation);
			try {
				insertRelationStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to insert relation " + relation.getId() + ".", e);
			}
			
			writeEntityTags(insertRelationTagStatement, relation);
			
			memberList = relation.getMemberList();
			for (int i = 0; i < memberList.size(); i++) {
				RelationMember member;
				
				member = memberList.get(i);
				try {
					prmIndex = 1;
					insertRelationMemberStatement.setLong(prmIndex++, relation.getId());
					insertRelationMemberStatement.setLong(prmIndex++, member.getMemberId());
					insertRelationMemberStatement.setByte(prmIndex++, memberTypeValueMapper.getMemberType(member.getMemberType()));
					insertRelationMemberStatement.setString(prmIndex++, member.getMemberRole());
					
					insertRelationMemberStatement.executeUpdate();
					
				} catch (SQLException e) {
					throw new OsmosisRuntimeException("Unable to insert a new relation member.", e);
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
		dbCtx.release();
	}
}
