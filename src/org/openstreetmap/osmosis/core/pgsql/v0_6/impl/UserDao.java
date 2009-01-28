// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.pgsql.common.BaseDao;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;
import org.openstreetmap.osmosis.core.pgsql.common.NoSuchRecordException;


/**
 * Performs all user-specific db operations.
 * 
 * @author Brett Henderson
 */
public class UserDao extends BaseDao {
	private static final String SELECT_USER = "SELECT id, name FROM users WHERE id = ?";
	private static final String INSERT_USER = "INSERT INTO users(id, name) VALUES(?, ?)";
	private static final String UPDATE_USER = "UPDATE users SET name = ? WHERE id = ?";
	
	private PreparedStatement selectUserStatement;
	private PreparedStatement insertUserStatement;
	private PreparedStatement updateUserStatement;
	private ActionDao actionDao;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public UserDao(DatabaseContext dbCtx, ActionDao actionDao) {
		super(dbCtx);
		
		this.actionDao = actionDao;
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
				resultSet.getString("name")
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
		
		if (selectUserStatement == null) {
			selectUserStatement = prepareStatement(SELECT_USER);
		}
		
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
		
		if (insertUserStatement == null) {
			insertUserStatement = prepareStatement(INSERT_USER);
		}
		
		prmIndex = 1;
		
		try {
			insertUserStatement.setInt(prmIndex++, user.getId());
			insertUserStatement.setString(prmIndex++, user.getName());
			
			insertUserStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to insert user " + user.getId() + ".", e);
		}
		
		actionDao.addAction(ActionDataType.USER, ChangesetAction.CREATE, user.getId());
	}
	
	
	/**
	 * Updates the specified user record in the database.
	 * 
	 * @param user
	 *            The user to update.
	 */
	public void updateUser(OsmUser user) {
		int prmIndex;
		
		if (updateUserStatement == null) {
			updateUserStatement = prepareStatement(UPDATE_USER);
		}
		
		prmIndex = 1;
		try {
			updateUserStatement.setString(prmIndex++, user.getName());
			updateUserStatement.setInt(prmIndex++, user.getId());
			
			updateUserStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to update user " + user.getId() + ".", e);
		}
		
		actionDao.addAction(ActionDataType.USER, ChangesetAction.MODIFY, user.getId());
	}
}
