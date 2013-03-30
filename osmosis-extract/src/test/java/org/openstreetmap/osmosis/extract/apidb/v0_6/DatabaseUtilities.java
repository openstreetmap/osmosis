// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.extract.apidb.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.database.AuthenticationPropertiesLoader;
import org.openstreetmap.osmosis.core.database.DatabaseConstants;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.testutil.TestDataUtilities;


/**
 * Contains re-usable functionality for manipulating the database during tests.
 * 
 * @author Brett Henderson
 */
public class DatabaseUtilities {
	private static final String AUTHFILE = "v0_6/apidb-authfile.txt";
	private static final String AUTHFILE_PROPERTY = "db.apidb.authfile";
	
	private TestDataUtilities dataUtils;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dataUtils
	 *            The test data manager.
	 */
	public DatabaseUtilities(TestDataUtilities dataUtils) {
		this.dataUtils = dataUtils;
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
        credentialsLoader = new AuthenticationPropertiesLoader(getAuthorizationFile());
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
        		"authFile=" + getAuthorizationFile().getPath(),
        		"allowIncorrectSchemaVersion=true"
        		});
    }
    
    
    /**
	 * Returns the location of the database authorization file.
	 * 
	 * @return The authorization file.
	 */
    public File getAuthorizationFile() {
    	return dataUtils.createDataFile(AUTHFILE_PROPERTY, AUTHFILE);
    }
}
