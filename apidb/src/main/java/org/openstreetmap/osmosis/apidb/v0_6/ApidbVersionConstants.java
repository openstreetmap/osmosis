// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6;


/**
 * Defines constants specific to the specific schema version.
 * 
 * @author Brett Henderson
 */
public final class ApidbVersionConstants {
	
	/**
	 * This class cannot be instantiated.
	 */
	private ApidbVersionConstants() {
	}
	
	/**
	 * Defines the schema migrations expected to be in the database.
	 */
	public static final String[] SCHEMA_MIGRATIONS = {
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
		"21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
		"31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
		"41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
		"51", "52", "20100513171259", "20100516124737",
		"20100910084426", "20101114011429", "20110322001319", 
		"20110925112722", "20111116184519", "20111212183945",
		"20120208122334", "20120208194454", "20120123184321",
		"20120219161649", "20120214210114", "20120328090602",
		"20120404205604", "20120318201948", "20120808231205"
	};
}
