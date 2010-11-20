// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;


/**
 * Defines the values for the "action" columns in the pgsql schema. These
 * actions define what activity has been performed on an entity during
 * application of a changeset.
 * 
 * @author Brett Henderson
 */
public enum ChangesetAction {
	/**
	 * No entity is unchanged.
	 */
	NONE("N"),
	/**
	 * The entity has been added.
	 */
	CREATE("C"),
	/**
	 * The entity has been modified.
	 */
	MODIFY("M"),
	/**
	 * The entity has been deleted.
	 */
	DELETE("D");
	
	
	private final String dbValue;
	
	
	private ChangesetAction(String dbValue) {
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
