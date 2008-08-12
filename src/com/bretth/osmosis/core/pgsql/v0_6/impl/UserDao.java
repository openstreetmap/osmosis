// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.NoSuchRecordException;
import com.bretth.osmosis.core.store.Releasable;


/**
 * Performs all user-specific db operations.
 * 
 * @author Brett Henderson
 */
public class UserDao implements Releasable {
	private static final String SELECT_USER = "SELECT id, user_name FROM users WHERE id = ?";
	private static final String INSERT_USER = "INSERT INTO users(id, user_name) VALUES(?, ?)";
	
	private DatabaseContext dbCtx;
	private PreparedStatement selectUserStatement;
	private PreparedStatement insertUserStatement;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public UserDao(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		initialized = false;
	}
	
	
	private void initialize() {
		if (!initialized) {
			selectUserStatement = dbCtx.prepareStatement(SELECT_USER);
			insertUserStatement = dbCtx.prepareStatement(INSERT_USER);
			
			initialized = true;
		}
	}
	
	
	/**
	 * Builds a user from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded user.
	 */
	private OsmUser buildUser(ResultSet resultSet) {
		try {
			return OsmUser.getInstance(
				resultSet.getString("user_name"),
				resultSet.getInt("id")
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a user from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Loads the specified way from the database.
	 * 
	 * @param userId
	 *            The unique identifier of the user.
	 * @return The loaded user.
	 */
	public OsmUser getUser(long userId) {
		ResultSet resultSet = null;
		OsmUser user;
		
		initialize();
		
		try {
			selectUserStatement.setLong(1, userId);
			
			resultSet = selectUserStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new NoSuchRecordException("User " + userId + " doesn't exist.");
			}
			user = buildUser(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			return user;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Query failed for user " + userId + ".");
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
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (selectUserStatement != null) {
			try {
				selectUserStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
		}
		if (insertUserStatement != null) {
			try {
				insertUserStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
		}
	}
}
