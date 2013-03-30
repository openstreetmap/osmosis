// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;


/**
 * Defines common constants shared between database tasks.
 * 
 * @author Brett Henderson
 */
public final class DatabaseConstants {
	
	/**
	 * This class cannot be instantiated.
	 */
	private DatabaseConstants() {
	}
	

    /**
     * The task argument for specifying an database authorisation properties file.
     */
    public static final String TASK_ARG_AUTH_FILE = "authFile";

    /**
     * The task argument for specifying the host for a database connection.
     */
    public static final String TASK_ARG_HOST = "host";

    /**
     * The task argument for specifying the database instance for a database connection.
     */
    public static final String TASK_ARG_DATABASE = "database";

    /**
     * The task argument for specifying the user for a database connection.
     */
    public static final String TASK_ARG_USER = "user";
    
    /**
     * The task argument for specifying the database type to be used.
     */
    public static final String TASK_ARG_DB_TYPE = "dbType";

    /**
     * The task argument for specifying the password for a database connection.
     */
    public static final String TASK_ARG_PASSWORD = "password";

    /**
     * The task argument for specifying whether schema version validation should be performed.
     */
    public static final String TASK_ARG_VALIDATE_SCHEMA_VERSION = "validateSchemaVersion";

    /**
     * The task argument for specifying what should occur if an invalid schema version is
     * encountered.
     */
    public static final String TASK_ARG_ALLOW_INCORRECT_SCHEMA_VERSION = "allowIncorrectSchemaVersion";

    /**
     * The task argument for forcing a utf-8 database connection.
     */
    public static final String TASK_ARG_FORCE_UTF8 = "forceUtf8";

    /**
     * The task argument for enabling profiling on the database connection.
     */
    public static final String TASK_ARG_PROFILE_SQL = "profileSql";

    /**
     * The default host for a database connection.
     */
    public static final String TASK_DEFAULT_HOST = "localhost";

    /**
     * The default database for a database connection.
     */
    public static final String TASK_DEFAULT_DATABASE = "osm";

    /**
     * The default user for a database connection.
     */
    public static final String TASK_DEFAULT_USER = null;

    /**
     * The default password for a database connection.
     */
    public static final DatabaseType TASK_DEFAULT_DB_TYPE = DatabaseType.POSTGRESQL;

    /**
     * The default password for a database connection.
     */
    public static final String TASK_DEFAULT_PASSWORD = null;

    /**
     * The default value for whether schema version validation should be performed.
     */
    public static final boolean TASK_DEFAULT_VALIDATE_SCHEMA_VERSION = true;

    /**
     * The default value for whether the program should allow an incorrect schema version.
     */
    public static final boolean TASK_ALLOW_INCORRECT_SCHEMA_VERSION = false;

    /**
     * The default value for forcing a utf-8 connection.
     */
    public static final boolean TASK_DEFAULT_FORCE_UTF8 = false;

    /**
     * The default value for enabling profile on a database connection.
     */
    public static final boolean TASK_DEFAULT_PROFILE_SQL = false;
}
