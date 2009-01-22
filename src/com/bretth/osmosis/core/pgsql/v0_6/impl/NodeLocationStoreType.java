package com.bretth.osmosis.core.pgsql.v0_6.impl;


/**
 * Defines the different node location store implementations available.
 * 
 * @author Brett Henderson
 */
public enum NodeLocationStoreType {
	/**
	 * An in-memory node location store holds all information in memory.  This typically requires a very large JVM heap space.
	 */
	InMemory,
	
	/**
	 * A temporary file based node location store holds all information in a temporary file on disk.
	 */
	TempFile
}
