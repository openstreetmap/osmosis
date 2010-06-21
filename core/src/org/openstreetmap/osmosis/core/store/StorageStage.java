// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Each class used for managing storage has a lifecycle. The values of this enum
 * represent each state in that lifecycle.
 * 
 * @author Brett Henderson
 */
public enum StorageStage {
	/**
	 * No data has yet been written to storage.
	 */
	NotStarted,
	
	/**
	 * Data is being written to storage.
	 */
	Add,
	
	/**
	 * Data is being read from storage.
	 */
	Reading,
	
	/**
	 * All resources associated with the storage have been released.
	 */
	Released
}
