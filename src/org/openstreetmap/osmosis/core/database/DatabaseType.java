/**
 * 
 */
package org.openstreetmap.osmosis.core.database;

public enum DatabaseType {
    POSTGRESQL, MYSQL, UNKNOWN;

    public static DatabaseType fromString(String property) {
        if (POSTGRESQL.toString().equalsIgnoreCase(property))
            return POSTGRESQL;
        if (MYSQL.toString().equalsIgnoreCase(property))
            return MYSQL;
        return UNKNOWN;
    }
}