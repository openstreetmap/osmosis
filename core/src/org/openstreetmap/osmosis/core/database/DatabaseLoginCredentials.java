// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

/**
 * Stores all information required to connect to a database.
 * 
 * @author Brett Henderson
 */
public class DatabaseLoginCredentials {

	private String datasourceJndiLocation;
    private String host;
    private String database;
    private String user;
    private String password;
    private boolean forceUtf8;
    private boolean profileSql;
    private DatabaseType dbType;
    
    
	/**
	 * Creates a new instance.
	 * 
	 * @param datasourceJndiLocation
	 *            The location of the data source in JNDI.
	 */
    public DatabaseLoginCredentials(String datasourceJndiLocation) {
    	this.datasourceJndiLocation = datasourceJndiLocation;
    }
    

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
	 * @param forceUtf8
	 *            If true, the database connection will be forced to use utf-8 instead of the
	 *            database default.
	 * @param profileSql
	 *            If true, profile logging will be enabled on the database connection causing all
	 *            queries to be logged to stderr.
	 * @param dbType
	 *            The database type.
	 */
    public DatabaseLoginCredentials(String host, String database, String user, String password, boolean forceUtf8,
            boolean profileSql, DatabaseType dbType) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.forceUtf8 = forceUtf8;
        this.profileSql = profileSql;
        this.dbType = dbType;
    }
    
    
	/**
	 * Gets the location of the datasource in JNDI. If null, new connections
	 * will need to be created using other parameters.
	 * 
	 * @return The datasource location in JNDI.
	 */
    public String getDatasourceJndiLocation() {
    	return datasourceJndiLocation;
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
     * @param host The new host.
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
     * @param database The new database.
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
     * @param user The new user.
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
     * @param password The new password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the force utf-8 flag.
     * 
     * @return The force utf-8 flag.
     */
    public boolean getForceUtf8() {
        return forceUtf8;
    }

    /**
     * Updates the force utf-8 flag.
     * 
     * @param forceUtf8 The new force utf-8 flag.
     */
    public void setForceUtf8(boolean forceUtf8) {
        this.forceUtf8 = forceUtf8;
    }

    /**
     * Returns the profile SQL flag.
     * 
     * @return The profile SQL flag.
     */
    public boolean getProfileSql() {
        return profileSql;
    }

    /**
     * Updates the profile SQL flag.
     * 
     * @param profileSql The new profile SQL flag.
     */
    public void setProfileSql(boolean profileSql) {
        this.profileSql = profileSql;
    }

    /**
     * Return database type.
     * 
     * @return database type
     */
    public DatabaseType getDbType() {
        return dbType;
    }

    /**
     * Updates database type.
     * 
     * @param dbType database type
     */
    public void setDbType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    
    /**
	 * Updates the database type.
	 * 
	 * @param property
	 *            The database type property.
	 */
    public void setDbType(String property) {
        this.dbType = DatabaseType.fromString(property);
    }
}
