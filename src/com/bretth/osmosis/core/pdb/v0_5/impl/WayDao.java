// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBWayNode;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao implements Releasable {
	private static final String SQL_SELECT_SINGLE_WAY = "SELECT id, tstamp, user_name FROM way where id=?";
	private static final String SQL_SELECT_SINGLE_WAY_TAG = "SELECT way_id AS entity_id, name, value FROM way_tag where way_id=?";
	private static final String SQL_SELECT_SINGLE_WAY_NODE = "SELECT way_id, node_id, sequence_id FROM way_node where way_id=? ORDER BY sequence_id";
	
	private DatabaseContext dbCtx;
	private PreparedStatement singleWayStatement;
	private PreparedStatement singleWayTagStatement;
	private PreparedStatement singleWayNodeStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public WayDao(DatabaseContext dbCtx) {
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
					resultSet.getString("name"),
					resultSet.getString("value")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a tag from the current recordset row.", e);
		} 
	}
	
	
	/**
	 * Builds a way node from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded way node.
	 */
	private DBWayNode buildWayNode(ResultSet resultSet) {
		try {
			return new DBWayNode(
				resultSet.getLong("way_id"),
				new WayNode(
					resultSet.getLong("node_id")
				),
				resultSet.getInt("sequence_id")
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a way node from the current recordset row.", e);
		} 
	}
	
	
	/**
	 * Builds a way from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded way.
	 */
	private Way buildWay(ResultSet resultSet) {
		try {
			return new Way(
				resultSet.getLong("id"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				resultSet.getString("user_name")
			);
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a way from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Loads the specified way from the database.
	 * 
	 * @param wayId
	 *            The unique identifier of the way.
	 * @return The loaded way.
	 */
	public Way getWay(long wayId) {
		ResultSet resultSet = null;
		Way way;
		
		if (singleWayStatement == null) {
			singleWayStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_WAY);
		}
		if (singleWayTagStatement == null) {
			singleWayTagStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_WAY_TAG);
		}
		if (singleWayNodeStatement == null) {
			singleWayNodeStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_WAY_NODE);
		}
		
		try {
			singleWayStatement.setLong(1, wayId);
			singleWayTagStatement.setLong(1, wayId);
			singleWayNodeStatement.setLong(1, wayId);
			
			resultSet = singleWayStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new OsmosisRuntimeException("Way " + wayId + " doesn't exist.");
			}
			way = buildWay(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			resultSet = singleWayTagStatement.executeQuery();
			while (resultSet.next()) {
				way.addTag(buildTag(resultSet).getTag());
			}
			
			resultSet.close();
			resultSet = null;
			
			resultSet = singleWayNodeStatement.executeQuery();
			while (resultSet.next()) {
				way.addWayNode(buildWayNode(resultSet).getWayNode());
			}
			
			resultSet.close();
			resultSet = null;
			
			return way;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Query failed for way " + wayId + ".");
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
	 * Returns an iterator providing access to all ways in the database.
	 * 
	 * @return The way iterator.
	 */
	public ReleasableIterator<Way> iterate() {
		return new WayReader(dbCtx);
	}
	
	
	/**
	 * Allows all data within a bounding box to be iterated across.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 * @param completeWays
	 *            If true, all ways within the ways will be returned even if
	 *            they lie outside the box.
	 * @return An iterator pointing to the start of the result data.
	 */
	public ReleasableIterator<Way> iterateBoundingBox(double left, double right, double top, double bottom, boolean completeWays) {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (singleWayStatement != null) {
			try {
				singleWayStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleWayStatement = null;
		}
		if (singleWayTagStatement != null) {
			try {
				singleWayTagStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleWayTagStatement = null;
		}
	}
}
