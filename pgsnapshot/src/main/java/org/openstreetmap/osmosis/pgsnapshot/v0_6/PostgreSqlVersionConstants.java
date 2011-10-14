// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6;


/**
 * Defines constants specific to the specific schema version.
 * 
 * @author Brett Henderson
 */
public final class PostgreSqlVersionConstants {
	
	private PostgreSqlVersionConstants() {
		// This class cannot be instantiated.
	}
	
	
	/**
	 * Defines the schema version number currently supported.
	 */
	public static final int SCHEMA_VERSION = 6;
}
