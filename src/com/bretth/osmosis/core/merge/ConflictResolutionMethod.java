package com.bretth.osmosis.core.merge;


/**
 * Defines the methods that can be used to resolve conflicts when two sources
 * contain the same element.
 */
public enum ConflictResolutionMethod {
	/**
	 * Select the entity with the latest timestamp.
	 */
	Timestamp,
	
	/**
	 * Select the entity from the latest input source.
	 */
	LatestSource
}
