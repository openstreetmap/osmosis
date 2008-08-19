// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.NoSuchRecordException;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Performs all node-specific db operations.
 * 
 * @author Brett Henderson
 */
public class NodeDao extends EntityDao {
	private static final String SQL_SELECT_SINGLE_NODE = NodeBuilder.SQL_SELECT + " WHERE id=?";
	private static final String SQL_SELECT_SINGLE_NODE_TAG = "SELECT node_id AS entity_id, k, v FROM node_tags WHERE node_id=?";
	
	private PreparedStatement singleNodeStatement;
	private PreparedStatement singleNodeTagStatement;
	private NodeBuilder nodeBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public NodeDao(DatabaseContext dbCtx) {
		super(dbCtx);
		
		nodeBuilder = new NodeBuilder();
	}
	
	
	/**
	 * Loads the specified node from the database.
	 * 
	 * @param nodeId
	 *            The unique identifier of the node.
	 * @return The loaded node.
	 */
	public Node getNode(long nodeId) {
		DatabaseContext dbCtx;
		ResultSet resultSet = null;
		Node node;
		
		dbCtx = getDatabaseContext();
		
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
				throw new NoSuchRecordException("Node " + nodeId + " doesn't exist.");
			}
			node = nodeBuilder.buildEntity(resultSet);
			
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
		return new NodeReader(getDatabaseContext());
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
