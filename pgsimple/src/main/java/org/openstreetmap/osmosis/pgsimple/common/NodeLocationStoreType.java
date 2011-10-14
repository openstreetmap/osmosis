// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;


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
	TempFile,
	
	/**
	 * A temporary file based node location store holds all information in a
	 * temporary file on disk. This is optimised for small datasets, and is less
	 * efficient for large datasets.
	 */
	CompactTempFile
}
