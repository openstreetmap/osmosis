// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;
import org.openstreetmap.osmosis.core.mysql.common.IdentityColumnValueLoader;


/**
 * Creates or loads the details of the Osmosis user in the database.
 * 
 * @author Brett Henderson
 */
public class UserIdManager implements Releasable {
	private static final String INSERT_SQL_USER =
		"INSERT INTO users ("
		+ "email, active, pass_crypt,"
		+ " creation_time, display_name, data_public,"
		+ " description, home_lat, home_lon, home_zoom,"
		+ " nearby, pass_salt"
		+ ") VALUES ("
		+ "'osmosis@bretth.com', 1, '00000000000000000000000000000000',"
		+ " NOW(), 'Osmosis System User', 1,"
		+ " 'System user for the Osmosis toolset.', 0, 0, 3,"
		+ " 50, '00000000')";
	
	private static final String SELECT_SQL_USER =
		"SELECT id FROM users WHERE email='osmosis@bretth.com'";
	
	
	private DatabaseContext dbCtx;
	private boolean idLoaded;
	private long loadedUserId;
	private IdentityColumnValueLoader identityLoader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public UserIdManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		idLoaded = false;
		
		identityLoader = new IdentityColumnValueLoader(dbCtx);
	}
	
	
	/**
	 * Creates a new Osmosis user in the database.
	 * 
	 * @return The id of the newly created user.
	 */
	private long createNewUser() {
		dbCtx.executeStatement(INSERT_SQL_USER);
		
		return identityLoader.getLastInsertId();
	}
	
	
	/**
	 * Returns the id of an existing Osmosis user from the database.
	 * 
	 * @return The id of an existing user, -1 if no user exists.
	 */
	private long getExistingUser() {
		try {
			ResultSet existingUserQuery;
			long userId;
			
			existingUserQuery = dbCtx.executeStreamingQuery(SELECT_SQL_USER);
			
			if (existingUserQuery.next()) {
				userId = existingUserQuery.getLong("id");
			} else {
				userId = -1;
			}
			
			existingUserQuery.close();
			
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
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		identityLoader.release();
	}
}
