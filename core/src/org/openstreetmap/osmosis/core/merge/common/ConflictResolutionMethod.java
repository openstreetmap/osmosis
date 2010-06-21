// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.common;


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
	LatestSource,
	
	/**
	 * Select the entity with the highest version number.
	 */
	Version
}
