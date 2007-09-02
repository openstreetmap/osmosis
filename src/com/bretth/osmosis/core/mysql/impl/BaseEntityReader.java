package com.bretth.osmosis.core.mysql.impl;


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
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public BaseEntityReader(String host, String database, String user, String password, boolean readAllUsers) {
		super(host, database, user, password);
		
		this.readAllUsers = readAllUsers;
	}
	
	
	/**
	 * Determines the appropriate user name to add to an entity based upon the
	 * user details provided.
	 * 
	 * @param dataPublic
	 *            The value of the public edit flag for the user.
	 * @param userName
	 *            The display name of the user.
	 * @return The appropriate user name to add to the entity.
	 */
	protected String readUserField(boolean dataPublic, String userName) {
		if (userName == null || userName.isEmpty()) {
			return "";
		} else if (readAllUsers || dataPublic) {
			return userName;
		} else {
			return "";
		}
	}
}
