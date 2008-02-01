// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Creates or loads the details of the Osmosis user in the database.
 * 
 * @author Brett Henderson
 */
public class UserIdManager {
	private static final String INSERT_SQL_USER =
		"INSERT INTO api_user (" +
		"email, name" +
		") VALUES (" +
		"'osmosis@bretth.com', 'Osmosis System User'" +
		")";
	
	private static final String SELECT_SQL_USER =
		"SELECT id FROM api_user WHERE email='osmosis@bretth.com'";
	
	private static final String SELECT_LAST_INSERT_ID =
		"SELECT currval('seq_user_id')";
	
	
	private DatabaseContext dbCtx;
	private boolean idLoaded;
	private long loadedUserId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public UserIdManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		idLoaded = false;
	}
	
	
	/**
	 * Returns the id of the most recently inserted row on the current
	 * connection.
	 * 
	 * @return The newly inserted id.
	 */
	private long getLastInsertId() {
		try {
			Statement statement;
			ResultSet lastInsertQuery;
			long lastInsertId;
			
			statement = dbCtx.createStatement();
			lastInsertQuery = statement.executeQuery(SELECT_LAST_INSERT_ID);
			
			lastInsertQuery.next();
			
			lastInsertId = lastInsertQuery.getLong(1);
			
			lastInsertQuery.close();
			statement.close();
			
			return lastInsertId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the id of the newly inserted user record.",
				e
			);
		}
	}
	
	
	/**
	 * Creates a new Osmosis user in the database.
	 * 
	 * @return The id of the newly created user.
	 */
	private long createNewUser() {
		dbCtx.executeStatement(INSERT_SQL_USER);
		
		return getLastInsertId();
	}
	
	
	/**
	 * Returns the id of an existing Osmosis user from the database.
	 * 
	 * @return The id of an existing user, -1 if no user exists.
	 */
	private long getExistingUser() {
		try {
			Statement statement;
			ResultSet existingUserQuery;
			long userId;
			
			statement = dbCtx.createStatement();
			existingUserQuery = statement.executeQuery(SELECT_SQL_USER);
			
			if (existingUserQuery.next()) {
				userId = existingUserQuery.getLong("id");
			} else {
				userId = -1;
			}
			
			existingUserQuery.close();
			statement.close();
			
			return userId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the id of an existing user record.",
				e
			);
		}
	}
	
	
	/**
	 * Returns the id of the Osmosis OSM user id in the database. It will create
	 * a new user if one doesn't exist.
	 * 
	 * @return The id of the user.
	 */
	public long getUserId() {
		if (!idLoaded) {
			long userId;
			
			// Retrieve the existing user if it exists.
			userId = getExistingUser();
			
			// If the user doesn't already exist, create a new one.
			if (userId < 0) {
				userId = createNewUser();
			}
			
			loadedUserId = userId;
			idLoaded = true;
		}
		
		return loadedUserId;
	}
}
