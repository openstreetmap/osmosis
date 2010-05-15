// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.common;


/**
 * Defines the different node location store implementations available.
 * 
 * @author Brett Henderson
 */
public enum NodeLocationStoreType {
	/**
	 * An in-memory node location store holds all information in memory. This
	 * typically requires a very large JVM heap space.
	 */
	InMemory,
	
	/**
	 * A temporary file based node location store holds all information in a temporary file on disk.
	 */
	TempFile
}
