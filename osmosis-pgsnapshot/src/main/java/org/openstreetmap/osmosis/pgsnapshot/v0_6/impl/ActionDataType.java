// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;


/**
 * Defines all the data types supported by the action table.
 * 
 * @author Brett Henderson
 */
public enum ActionDataType {
	/**
	 * A user record.
	 */
	USER("U"),
	/**
	 * A node entity.
	 */
	NODE("N"),
	/**
	 * A way entity.
	 */
	WAY("W"),
	/**
	 * A relation entity.
	 */
	RELATION("R");
	
	
	private final String dbValue;
	
	
	private ActionDataType(String dbValue) {
		this.dbValue = dbValue;
	}
	
	
	/**
	 * Returns the database value representing this action.
	 * 
	 * @return The database value.
	 */
	public String getDatabaseValue() {
		return dbValue;
	}
}
