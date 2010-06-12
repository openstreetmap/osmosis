// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;


/**
 * Defines the interface for all class implementations allowing a set of ids to
 * be marked as in use. This is used in filter tasks for tracking which entities
 * have been selected. Implementations support negative numbers.
 * 
 * @author Brett Henderson
 */
public interface IdTracker extends Iterable<Long> {
	/**
	 * Marks the specified id as active.
	 * 
	 * @param id
	 *            The identifier to be flagged.
	 */
	void set(long id);
	
	
	/**
	 * Checks whether the specified id is active.
	 * 
	 * @param id
	 *            The identifier to be checked.
	 * @return True if the identifier is active, false otherwise.
	 */
	boolean get(long id);
	
	
	/**
	 * Sets all the ids contained in the specified tracker.
	 * 
	 * @param idTracker
	 *            The id tracker containing the ids to set.
	 */
	void setAll(IdTracker idTracker);
}
