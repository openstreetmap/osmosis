// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.database;


/**
 * Stores all information required to connect to a database.
 * 
 * @author Brett Henderson
 */
public class DatabaseLoginCredentials {
	private String host;
	private String database;
	private String user;
	private String password;
	
	
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
	 */
	public DatabaseLoginCredentials(String host, String database, String user, String password) {
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
	}
	
	
	/**
	 * Returns the host.
	 * 
	 * @return The host.
	 */
	public String getHost() {
		return host;
	}
	
	
	/**
	 * Updates the host.
	 * 
	 * @param host
	 *            The new host.
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	
	/**
	 * Returns the database.
	 * 
	 * @return The database.
	 */
	public String getDatabase() {
		return database;
	}
	
	
	/**
	 * Updates the database.
	 * 
	 * @param database
	 *            The new database.
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
	
	
	/**
	 * Returns the user.
	 * 
	 * @return The user.
	 */
	public String getUser() {
		return user;
	}
	
	
	/**
	 * Updates the user.
	 * 
	 * @param user
	 *            The new user.
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	
	/**
	 * Returns the password.
	 * 
	 * @return The password.
	 */
	public String getPassword() {
		return password;
	}
	
	
	/**
	 * Updates the password.
	 * 
	 * @param password
	 *            The new password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
