// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgresql.geometric.PGpoint;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Performs all node-specific db operations.
 * 
 * @author Brett Henderson
 */
public class NodeDao implements Releasable {
	private static final String SQL_SELECT_SINGLE_NODE = "SELECT id, tstamp, user_id, user_name, geom FROM nodes WHERE id=?";
	private static final String SQL_SELECT_SINGLE_NODE_TAG = "SELECT node_id AS entity_id, k, v FROM node_tags WHERE node_id=?";
	
	private DatabaseContext dbCtx;
	private PreparedStatement singleNodeStatement;
	private PreparedStatement singleNodeTagStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public NodeDao(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
	}
	
	
	/**
	 * Builds a tag from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded tag.
	 */
	private DBEntityTag buildTag(ResultSet resultSet) {
		try {
			return new DBEntityTag(
				resultSet.getLong("entity_id"),
				new Tag(
					resultSet.getString("k"),
					resultSet.getString("v")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a tag from the current recordset row.", e);
		} 
	}
	
	
	/**
	 * Builds a node from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded node.
	 */
	private Node buildNode(ResultSet resultSet) {
		try {
			PGpoint coordinate;
			OsmUser user;
			
			coordinate = (PGpoint) resultSet.getObject("coordinate");
			
			if (resultSet.getInt("user_id") != OsmUser.NONE.getId()) {
				user = new OsmUser(resultSet.getInt("user_id"), resultSet.getString("user_name"));
			} else {
				user = OsmUser.NONE;
			}
			
			return new Node(
				resultSet.getLong("id"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				user,
				coordinate.y,
				coordinate.x
			);
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a node from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Loads the specified node from the database.
	 * 
	 * @param nodeId
	 *            The unique identifier of the node.
	 * @return The loaded node.
	 */
	public Node getNode(long nodeId) {
		ResultSet resultSet = null;
		Node node;
		
		if (singleNodeStatement == null) {
			singleNodeStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_NODE);
		}
		if (singleNodeTagStatement == null) {
			singleNodeTagStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_NODE_TAG);
		}
		
		try {
			singleNodeStatement.setLong(1, nodeId);
			singleNodeTagStatement.setLong(1, nodeId);
			
			resultSet = singleNodeStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new OsmosisRuntimeException("Node " + nodeId + " doesn't exist.");
			}
			node = buildNode(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			resultSet = singleNodeTagStatement.executeQuery();
			while (resultSet.next()) {
				node.addTag(buildTag(resultSet).getTag());
			}
			
			resultSet.close();
			resultSet = null;
			
			return node;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Query failed for node " + nodeId + ".");
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * Returns an iterator providing access to all nodes in the database.
	 * 
	 * @return The node iterator.
	 */
	public ReleasableIterator<Node> iterate() {
		return new NodeReader(dbCtx);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (singleNodeStatement != null) {
			try {
				singleNodeStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleNodeStatement = null;
		}
		if (singleNodeTagStatement != null) {
			try {
				singleNodeTagStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleNodeTagStatement = null;
		}
	}
}
