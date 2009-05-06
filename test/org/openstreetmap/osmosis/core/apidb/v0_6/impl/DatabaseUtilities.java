// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.io.File;

import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.AuthenticationPropertiesLoader;
import org.openstreetmap.osmosis.core.database.DatabaseConstants;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;

import data.util.DataFileUtilities;


/**
 * Contains re-usable functionality for manipulating the database during tests.
 * 
 * @author Brett Henderson
 */
public class DatabaseUtilities {
	private static final String AUTHFILE = "v0_6/apidb-authfile.txt";
	
	private DataFileUtilities fileUtils;
	
	
	/**
	 * Creates a new instance.
	 */
	public DatabaseUtilities() {
		fileUtils = new DataFileUtilities();
	}
	

	/**
	 * Creates a new database context pointing at the test database.
	 * 
	 * @return A fully configured database context.
	 */
    public DatabaseContext createDatabaseContext() {
        AuthenticationPropertiesLoader credentialsLoader;
        DatabaseLoginCredentials credentials;

        credentials = new DatabaseLoginCredentials(DatabaseConstants.TASK_DEFAULT_HOST,
                DatabaseConstants.TASK_DEFAULT_DATABASE, DatabaseConstants.TASK_DEFAULT_USER,
                DatabaseConstants.TASK_DEFAULT_PASSWORD, DatabaseConstants.TASK_DEFAULT_FORCE_UTF8,
                DatabaseConstants.TASK_DEFAULT_PROFILE_SQL, DatabaseConstants.TASK_DEFAULT_DB_TYPE);
        credentialsLoader = new AuthenticationPropertiesLoader(fileUtils.getDataFile("v0_6/apidb-authfile.txt"));
        credentialsLoader.updateLoginCredentials(credentials);
        return new DatabaseContext(credentials);
    }
    
 
    /**
     * Removes all data from the database.
     */
    public void truncateDatabase() {
    	// Remove all existing data from the database.
        Osmosis.run(new String[] {
        		"-q",
        		"--truncate-apidb-0.6",
        		"authFile=" + getAuthorizationFile().getPath()
        		});
    }
    
    
    /**
	 * Returns the location of the database authorization file.
	 * 
	 * @return The authorization file.
	 */
    public File getAuthorizationFile() {
    	return fileUtils.getDataFile(AUTHFILE);
    }
}
