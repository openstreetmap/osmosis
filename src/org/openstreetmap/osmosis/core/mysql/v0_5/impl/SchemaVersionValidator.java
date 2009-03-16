// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads the version number stored in the schema_info table and verifies that it
 * matches the expected version.
 * 
 * @author Brett Henderson
 */
public class SchemaVersionValidator {
	private static Logger log = Logger.getLogger(SchemaVersionValidator.class.getName());
	
	private static final String SELECT_SQL = "SELECT version FROM schema_info";
	
	private DatabasePreferences preferences;
	private DatabaseContext dbCtx;
	private boolean validated;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            The database preferences.
	 */
	public SchemaVersionValidator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.preferences = preferences;
		dbCtx = new DatabaseContext(loginCredentials);
	}
	
	
	/**
	 * Validates that the version number of the schema matches the expected
	 * version. This method caches the result allowing it to be called multiple
	 * times without a performance penalty.
	 * 
	 * @param expectedVersion
	 *            The expected version number.
	 */
	public void validateVersion(int expectedVersion) {
		if (!validated) {
			validateDBVersion(expectedVersion);
			
			validated = true;
		}
	}
	
	
	/**
	 * Performs the database lookup and validates the expected version.
	 * 
	 * @param expectedVersion
	 *            The expected version number.
	 */
	private void validateDBVersion(int expectedVersion) {
		if (preferences.getValidateSchemaVersion()) {
			try {
				ResultSet resultSet;
				int dbVersion;
				
				resultSet = dbCtx.executeStreamingQuery(SELECT_SQL);
				
				if (!resultSet.next()) {
					throw new OsmosisRuntimeException("No rows were found in the schema info table.");
				}
				
				dbVersion = resultSet.getInt("version");
				
				if (dbVersion != expectedVersion) {
					String message;
					
					message =
						"The database schema version of " + dbVersion
						+ " does not match the expected version of "
						+ expectedVersion + ".";
					
					if (preferences.getAllowIncorrectSchemaVersion()) {
						log.warning(message);
					} else {
						throw new OsmosisRuntimeException(message);
					}
				}
				
				resultSet.close();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Unable to read the schema version from the schema info table.", e);
			} finally {
				cleanup();
			}
		}
	}
	
	
	/**
	 * Releases all resources allocated during execution.
	 */
	private void cleanup() {
		dbCtx.release();
	}
}
