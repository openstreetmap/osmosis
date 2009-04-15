// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;


/**
 * Holds information about a changeset currently being used during entity writing to the database.
 * 
 * @author Brett Henderson
 */
public class ActiveChangeset {
	private long changesetId;
	private int entityCount;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param changesetId
	 *            The id of the changeset.
	 */
	public ActiveChangeset(long changesetId) {
		this.changesetId = changesetId;
		
		entityCount = 0;
	}
	
	
	/**
	 * Gets the database identifier of the changeset.
	 * 
	 * @return The changeset id.
	 */
	public long getChangesetId() {
		return changesetId;
	}
	
	
	/**
	 * Gets the number of entities that have been added using this changeset.
	 * 
	 * @return The entity count.
	 */
	public int getEntityCount() {
		return entityCount;
	}
	
	
	/**
	 * Adds 1 to the current entity count.
	 */
	public void incrementEntityCount() {
		entityCount++;
	}
}
