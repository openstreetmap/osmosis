// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.domain.v0_6;


/**
 * An enum representing the different data types in the OSM data model.
 * 
 * @author Brett Henderson
 */
public enum EntityType {
	/**
	 * Representation of the latitude/longitude bounding box of the entity stream.
	 */
	Bound,
	
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
