package com.bretth.osmosis.core.filter.common;


/**
 * Defines the interface for all class implementations allowing a set of ids to
 * be marked as in use. This is used in filter tasks for tracking which entities
 * have been selected. Implementations support negative numbers.
 * 
 * @author Brett Henderson
 */
public interface IdTracker {
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
}
