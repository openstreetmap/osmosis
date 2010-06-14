// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;

/**
 * Reads the version number stored in the schema_info table and verifies that it matches the
 * expected version.
 * 
 * @author Brett Henderson
 */
public class SchemaVersionValidator {

    private static Logger log = Logger.getLogger(SchemaVersionValidator.class.getName());

    private static final String SELECT_SQL = "SELECT version FROM schema_migrations";

    private final DatabasePreferences preferences;

    private final DatabaseContext dbCtx;

    private boolean validated;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param preferences The database preferences.
     */
    public SchemaVersionValidator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
        this.preferences = preferences;

        dbCtx = new DatabaseContext(loginCredentials);
    }

    /**
     * Validates that the schema migrations match the expected list of migrations. This method
     * caches the result allowing it to be called multiple times without a performance penalty.
     * 
     * @param expectedMigrations The expected schema migrations.
     */
    public void validateVersion(String[] expectedMigrations) {
        if (!validated) {
            validateDBVersion(expectedMigrations);

            validated = true;
        }
    }

    /**
     * Performs the database lookup and validates the expected version.
     * 
     * @param expectedMigrations The expected schema migrations.
     */
    private void validateDBVersion(String[] expectedMigrations) {
        if (preferences.getValidateSchemaVersion()) {
            try {
                ResultSet resultSet;
                Set<String> actualMigrationSet;
                Set<String> expectedMigrationSet;
                List<String> matchingMigrations;

                // Load the expected migrations into a Set.
                expectedMigrationSet = new HashSet<String>();
                for (String expectedMigration : expectedMigrations) {
                    expectedMigrationSet.add(expectedMigration);
                }

                // Load the database migrations into a Set.
                actualMigrationSet = new HashSet<String>();
                resultSet = dbCtx.executeQuery(SELECT_SQL);
                while (resultSet.next()) {
                    actualMigrationSet.add(resultSet.getString("version"));
                }
                resultSet.close();

                // Remove items from both sets that are identical.
                matchingMigrations = new ArrayList<String>();
                for (String migration : expectedMigrationSet) {
                    if (actualMigrationSet.contains(migration)) {
                        matchingMigrations.add(migration);
                    }
                }
                for (String migration : matchingMigrations) {
                    expectedMigrationSet.remove(migration);
                    actualMigrationSet.remove(migration);
                }

                // If either Set contains elements, we have a schema version mismatch.
                if (expectedMigrationSet.size() > 0 || actualMigrationSet.size() > 0) {
                    StringBuilder errorMessage;

                    errorMessage = new StringBuilder();

                    errorMessage.append("Database version mismatch.");
                    if (expectedMigrationSet.size() > 0) {
                        errorMessage.append(" The schema is missing migrations " + expectedMigrationSet
                                + ", may need to upgrade schema or specify validateSchemaVersion=no.");
                    }
                    if (actualMigrationSet.size() > 0) {
                        errorMessage.append(" The schema contains unexpected migrations " + actualMigrationSet
                                + ", may need to upgrade osmosis or specify validateSchemaVersion=no.");
                    }

                    if (preferences.getAllowIncorrectSchemaVersion()) {
                        log.warning(errorMessage.toString());
                    } else {
                        throw new OsmosisRuntimeException(errorMessage.toString());
                    }
                }

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
