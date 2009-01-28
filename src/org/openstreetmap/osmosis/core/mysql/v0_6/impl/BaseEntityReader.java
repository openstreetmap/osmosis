// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6.impl;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.mysql.common.BaseTableReader;


/**
 * Provides the base implementation of all database entity readers. This extends
 * the base table reader with additional features used by top level entity
 * readers.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The type of entity to retrieved.
 */
public abstract class BaseEntityReader<T> extends BaseTableReader<T> {
	
	private boolean readAllUsers;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public BaseEntityReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		super(loginCredentials);
		
		this.readAllUsers = readAllUsers;
	}


	/**
	 * Determines the appropriate user name to add to an entity based upon the
	 * user details provided.
	 * 
	 * @param dataPublic
	 *            The value of the public edit flag for the user.
	 * @param userId
	 *            The unique id of the user.
	 * @param userName
	 *            The display name of the user.
	 * @return The appropriate user to add to the entity.
	 */
	protected OsmUser readUserField(boolean dataPublic, int userId, String userName) {
		if (userId == OsmUser.NONE.getId()) {
			return OsmUser.NONE;
		} else if (dataPublic || readAllUsers) {
			String correctedUserName;
			
			if (userName == null) {
				correctedUserName = "";
			} else {
				correctedUserName = userName;
			}
			
			return new OsmUser(userId, correctedUserName);
		} else {
			return OsmUser.NONE;
		}
	}
}
