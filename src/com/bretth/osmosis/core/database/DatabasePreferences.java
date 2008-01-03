// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.database;


/**
 * Stores parameters that can be used to configure database behaviour.
 * 
 * @author Brett Henderson
 */
public class DatabasePreferences {
	private boolean validateSchemaVersion;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param validateSchemaVersion
	 *            Specifies whether a schema version check should be performed.
	 */
	public DatabasePreferences(boolean validateSchemaVersion) {
		this.validateSchemaVersion = validateSchemaVersion;
	}
	
	
	/**
	 * Returns the validateSchemaVersion flag.
	 * 
	 * @return The validateSchemaVersion value.
	 */
	public boolean getValidateSchemaVersion() {
		return validateSchemaVersion;
	}
	
	
	/**
	 * Updates the validateSchemaVersion flag.
	 * 
	 * @param validateSchemaVersion
	 *            The new validateSchemaVersion value.
	 */
	public void setValidateSchemaVersion(boolean validateSchemaVersion) {
		this.validateSchemaVersion = validateSchemaVersion;
	}
}
