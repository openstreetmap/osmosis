// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Reads the version number stored in the schema_info table and verifies that it
 * matches the expected version.
 * 
 * @author Brett Henderson
 */
public class SchemaVersionValidator {
	private static final String SELECT_SQL = "SELECT version FROM schema_info";
	
	private DatabasePreferences preferences;
	private JdbcTemplate jdbcTemplate;
	private boolean validated;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Provides access to the database.
	 * @param preferences
	 *            The database preferences.
	 */
	public SchemaVersionValidator(JdbcTemplate jdbcTemplate, DatabasePreferences preferences) {
		this.jdbcTemplate = jdbcTemplate;
		this.preferences = preferences;
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
			int dbVersion;
			
			dbVersion = jdbcTemplate.queryForInt(SELECT_SQL);
			
			if (dbVersion != expectedVersion) {
				throw new OsmosisRuntimeException(
					"The database schema version of " + dbVersion
					+ " does not match the expected version of " + expectedVersion + "."
				);
			}
		}
	}
}
