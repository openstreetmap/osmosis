// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6;


/**
 * Defines constants specific to the specific schema version.
 * 
 * @author Brett Henderson
 */
public interface MySqlVersionConstants {
	/**
	 * Defines the schema migrations expected to be in the database.
	 */
	String[] SCHEMA_MIGRATIONS = {
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
		"21", "22", "23"//, "24"
	};
}
