// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.ReleasableStatementContainer;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.store.Releasable;


/**
 * Creates or loads the details of the Osmosis user in the database.
 * 
 * @author Brett Henderson
 */
public class UserManager implements Releasable {
	private static final String SELECT_SQL_USER_EXISTS =
		"SELECT Count(id) AS userCount FROM users WHERE id = ?";
	private static final String INSERT_SQL_USER =
		"INSERT INTO users (" +
		"email, active, pass_crypt," +
		" creation_time, display_name, data_public," +
		" description, home_lat, home_lon, home_zoom," +
		" nearby, pass_salt" +
		") VALUES (" +
		"?, 1, '00000000000000000000000000000000'," +
		" NOW(), ?, ?," +
		" ?, 0, 0, 3," +
		" 50, '00000000')";
	private static final String UPDATE_SQL_USER =
		"UPDATE users SET display_name = ? WHERE id = ?";
	
	
	private DatabaseContext dbCtx;
	private Set<Integer> updatedUsers;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement statementInsert;
	private PreparedStatement statementExists;
	private PreparedStatement statementUpdate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public UserManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		updatedUsers = new HashSet<Integer>();
		statementContainer = new ReleasableStatementContainer();
	}


	/**
	 * Checks if the specified user exists in the database.
	 * 
	 * @param user
	 *            The user to check for.
	 * @return True if the user exists, false otherwise.
	 */
	private boolean doesUserExistInDb(OsmUser user) {
		int prmIndex;
		ResultSet resultSet;
		
		if (statementExists == null) {
			statementExists = statementContainer.add(dbCtx.prepareStatementForStreaming(SELECT_SQL_USER_EXISTS));
		}
		
		resultSet = null;
		try {
			boolean result;
			
			prmIndex = 1;
			statementExists.setInt(prmIndex++, user.getId());
			
			resultSet = statementExists.executeQuery();
			resultSet.next();
			
			if (resultSet.getInt("userCount") == 0) {
				result = false;
			} else {
				result = true;
			}
			
			resultSet.close();
			resultSet = null;
			
			return result;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to check if user with id " + user.getId() + " exists in the database.", e);
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
	 * Inserts the specified user into the database.
	 * 
	 * @param user
	 *            The user to be inserted.
	 */
	private void insertUser(OsmUser user) {
		int prmIndex;
		
		if (statementInsert == null) {
			statementInsert = statementContainer.add(dbCtx.prepareStatement(INSERT_SQL_USER));
		}
		
		try {
			prmIndex = 1;
			statementInsert.setString(prmIndex++, "osmosis_user_" + user.getId() + "@bretth.com");
			statementInsert.setString(prmIndex++, user.getName());
			statementInsert.setInt(prmIndex++, OsmUser.NONE.equals(user) ? 0 : 1);
			statementInsert.setString(prmIndex++, user.getName());
			
			statementInsert.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert user with id " + user.getId() + " into the database.", e);
		}
	}
	
	
	/**
	 * Updates the specified user in the database.
	 * 
	 * @param user
	 *            The user to be updated.
	 */
	private void updateUser(OsmUser user) {
		int prmIndex;
		
		if (statementUpdate == null) {
			statementUpdate = statementContainer.add(dbCtx.prepareStatement(UPDATE_SQL_USER));
		}
		
		try {
			prmIndex = 1;
			statementUpdate.setString(prmIndex++, user.getName());
			statementUpdate.setInt(prmIndex++, user.getId());
			
			statementUpdate.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to update user with id " + user.getId() + " in the database.", e);
		}
	}
	
	
	/**
	 * Adds the user to the database or updates the name of the existing
	 * database entry if one already exists with the same id.
	 * 
	 * @param user
	 *            The user to be created or updated.
	 */
	public void addOrUpdateUser(OsmUser user) {
		if (!updatedUsers.contains(user.getId())) {
			if (doesUserExistInDb(user)) {
				updateUser(user);
			} else {
				insertUser(user);
			}
			
			updatedUsers.add(user.getId());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
	}
}
