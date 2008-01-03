// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.merge.common;


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
