// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;


/**
 * Defines constants specific to the specific schema version.
 * 
 * @author Brett Henderson
 */
public interface ApidbVersionConstants {
	/**
	 * Defines the schema migrations expected to be in the database.
	 */
	String[] SCHEMA_MIGRATIONS = {
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
		"21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
		"31", "32", "33", "34", "35", "36", "37"
	};
}
