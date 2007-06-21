package com.bretth.osmosis.data;


/**
 * An enum representing the different data types in the OSM data model.
 * 
 * @author Brett Henderson
 */
public enum ElementType {
	/**
	 * Represents a geographical point.
	 */
	Node,
	
	/**
	 * Represents a connection between two nodes.
	 */
	Segment,
	
	/**
	 * Represents a set of segments forming a path.
	 */
	Way
}
