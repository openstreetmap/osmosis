// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_5;


/**
 * An enum representing the different data types in the OSM data model.
 * 
 * @author Brett Henderson
 */
public enum EntityType {
	/**
	 * Represents a geographical point.
	 */
	Node,
	
	/**
	 * Represents a set of segments forming a path.
	 */
	Way,
	
	/**
	 * Represents a relationship between multiple entities.
	 */
	Relation
}
