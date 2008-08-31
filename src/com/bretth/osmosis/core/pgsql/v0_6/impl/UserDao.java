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
	private static final String SELECT_USER = "SELECT id, name FROM users WHERE id = ?";
	private static final String INSERT_USER = "INSERT INTO users(id, name, action) VALUES(?, ?, ?)";
	private static final String UPDATE_USER = "UPDATE users SET name = ?, action = ? WHERE id = ?";
	
	private DatabaseContext dbCtx;
	private PreparedStatement selectUserStatement;
	private PreparedStatement insertUserStatement;
	private PreparedStatement updateUserStatement;
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
			updateUserStatement = dbCtx.prepareStatement(UPDATE_USER);
			
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
			return new OsmUser(
				resultSet.getInt("id"),
				resultSet.getString("user_name")
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
	 * Adds the specified user to the database.
	 * 
	 * @param user
	 *            The user to add.
	 */
	public void addUser(OsmUser user) {
		int prmIndex;
		
		prmIndex = 0;
		
		try {
			insertUserStatement.setInt(prmIndex++, user.getId());
			insertUserStatement.setString(prmIndex++, user.getName());
			insertUserStatement.setString(prmIndex++, ChangesetAction.ADD.getDatabaseValue());
			
			insertUserStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to insert user " + user.getId() + ".", e);
		}
	}
	
	
	/**
	 * Updates the specified user record in the database.
	 * 
	 * @param user
	 *            The user to update.
	 */
	public void updateUser(OsmUser user) {
		int prmIndex;
		
		prmIndex = 0;
		
		try {
			updateUserStatement.setString(prmIndex++, user.getName());
			updateUserStatement.setInt(prmIndex++, user.getId());
			updateUserStatement.setString(prmIndex++, ChangesetAction.MODIFY.getDatabaseValue());
			
			updateUserStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to update user " + user.getId() + ".", e);
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
		if (updateUserStatement != null) {
			try {
				updateUserStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
		}
	}
}
