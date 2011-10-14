// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

/**
 * Represents the different database types.
 */
public enum DatabaseType {
	/**
	 * The PostgreSQL database.
	 */
	POSTGRESQL,

	/**
	 * The MySQL database.
	 */
	MYSQL;
	

	/**
	 * Gets a database type value based on a string name.
	 * 
	 * @param name
	 *            The database type string.
	 * @return The strongly typed database type.
	 */
    public static DatabaseType fromString(String name) {
        if (POSTGRESQL.toString().equalsIgnoreCase(name)) {
            return POSTGRESQL;
        } else if (MYSQL.toString().equalsIgnoreCase(name)) {
            return MYSQL;
        } else {
        	throw new OsmosisRuntimeException("The database type name " + name + " is not recognized.");
        }
    }
}
