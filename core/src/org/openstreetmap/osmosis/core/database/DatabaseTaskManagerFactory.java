// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;

/**
 * Extends the basic task manager factory functionality with MySQL task specific common methods.
 * 
 * @author Brett Henderson
 */
public abstract class DatabaseTaskManagerFactory extends TaskManagerFactory {

    /**
     * Utility method for retrieving the login credentials for a database connection.
     * 
     * @param taskConfig Contains all information required to instantiate and configure the task.
     * @return The credentials for the database connection.
     */
    protected DatabaseLoginCredentials getDatabaseLoginCredentials(TaskConfiguration taskConfig) {
        DatabaseLoginCredentials loginCredentials;

        // Create a new credential object with default values.
        loginCredentials = new DatabaseLoginCredentials(DatabaseConstants.TASK_DEFAULT_HOST,
                DatabaseConstants.TASK_DEFAULT_DATABASE, DatabaseConstants.TASK_DEFAULT_USER,
                DatabaseConstants.TASK_DEFAULT_PASSWORD, DatabaseConstants.TASK_DEFAULT_FORCE_UTF8,
                DatabaseConstants.TASK_DEFAULT_PROFILE_SQL, DatabaseConstants.TASK_DEFAULT_DB_TYPE);

        // If an authentication properties file has been supplied, load override
        // values from there.
        if (doesArgumentExist(taskConfig, DatabaseConstants.TASK_ARG_AUTH_FILE)) {
            AuthenticationPropertiesLoader authLoader;

            authLoader = new AuthenticationPropertiesLoader(new File(getStringArgument(taskConfig,
                    DatabaseConstants.TASK_ARG_AUTH_FILE)));

            authLoader.updateLoginCredentials(loginCredentials);
        }

        // Update the credentials with any explicit arguments provided on the
        // command line.
        loginCredentials.setHost(getStringArgument(taskConfig, DatabaseConstants.TASK_ARG_HOST, loginCredentials
                .getHost()));
        loginCredentials.setDatabase(getStringArgument(taskConfig, DatabaseConstants.TASK_ARG_DATABASE,
                loginCredentials.getDatabase()));
        loginCredentials.setUser(getStringArgument(taskConfig, DatabaseConstants.TASK_ARG_USER, loginCredentials
                .getUser()));
        loginCredentials.setPassword(getStringArgument(taskConfig, DatabaseConstants.TASK_ARG_PASSWORD,
                loginCredentials.getPassword()));
        loginCredentials.setForceUtf8(getBooleanArgument(taskConfig, DatabaseConstants.TASK_ARG_FORCE_UTF8,
                loginCredentials.getForceUtf8()));
        loginCredentials.setProfileSql(getBooleanArgument(taskConfig, DatabaseConstants.TASK_ARG_PROFILE_SQL,
                loginCredentials.getProfileSql()));
        loginCredentials.setDbType(getStringArgument(taskConfig, DatabaseConstants.TASK_ARG_DB_TYPE, loginCredentials
				.getDbType().toString()));

        return loginCredentials;
    }

    /**
     * Utility method for retrieving the login credentials for a database connection.
     * 
     * @param taskConfig Contains all information required to instantiate and configure the task.
     * @return The value of the argument.
     */
    protected DatabasePreferences getDatabasePreferences(TaskConfiguration taskConfig) {
        DatabasePreferences preferences;

        // Create a new preferences object with default values.
        preferences = new DatabasePreferences(DatabaseConstants.TASK_DEFAULT_VALIDATE_SCHEMA_VERSION,
                DatabaseConstants.TASK_ALLOW_INCORRECT_SCHEMA_VERSION);

        // Update the preferences with any explicit arguments provided on the
        // command line.
        preferences.setValidateSchemaVersion(getBooleanArgument(taskConfig,
                DatabaseConstants.TASK_ARG_VALIDATE_SCHEMA_VERSION, preferences.getValidateSchemaVersion()));
        preferences
                .setAllowIncorrectSchemaVersion(getBooleanArgument(taskConfig,
                        DatabaseConstants.TASK_ARG_ALLOW_INCORRECT_SCHEMA_VERSION, preferences
                                .getAllowIncorrectSchemaVersion()));

        return preferences;
    }
}
