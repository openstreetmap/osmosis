// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;


/**
 * Stores parameters that can be used to configure database behaviour.
 * 
 * @author Brett Henderson
 */
public class DatabasePreferences {
	private boolean validateSchemaVersion;
	private boolean allowIncorrectSchemaVersion;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param validateSchemaVersion
	 *            Specifies whether a schema version check should be performed.
	 * @param allowIncorrectSchemaVersion
	 *            Defines behaviour on an incorrect schema version. If true, a
	 *            warning will be logged. If false, execution will abort.
	 */
	public DatabasePreferences(boolean validateSchemaVersion, boolean allowIncorrectSchemaVersion) {
		this.validateSchemaVersion = validateSchemaVersion;
		this.allowIncorrectSchemaVersion = allowIncorrectSchemaVersion;
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
	
	
	/**
	 * Returns the allowIncorrectSchemaVersion flag.
	 * 
	 * @return The allowIncorrectSchemaVersion value.
	 */
	public boolean getAllowIncorrectSchemaVersion() {
		return allowIncorrectSchemaVersion;
	}
	
	
	/**
	 * Updates the allowIncorrectSchemaVersion flag.
	 * 
	 * @param allowIncorrectSchemaVersion
	 *            The new allowIncorrectSchemaVersion value.
	 */
	public void setAllowIncorrectSchemaVersion(boolean allowIncorrectSchemaVersion) {
		this.allowIncorrectSchemaVersion = allowIncorrectSchemaVersion;
	}
}
