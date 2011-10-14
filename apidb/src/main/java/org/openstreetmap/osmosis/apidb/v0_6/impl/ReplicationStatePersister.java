// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

/**
 * Implementations of this interface provide persistence for replication state objects. This state
 * should be persisted after completion of the data replication, and preferably within the same
 * transaction to avoid duplicates in the face of failures.
 */
public interface ReplicationStatePersister {
	/**
	 * Persists the state.
	 * 
	 * @param state
	 *            The state to be persisted.
	 */
	void saveState(ReplicationState state);
	
	
	/**
	 * Loads the existing state.
	 * 
	 * @return The state to be loaded.
	 */
	ReplicationState loadState();
	
	
	/**
	 * Checks if state currently exists. If no state exists it will need to be initialized.
	 * 
	 * @return True if state exists, false otherwise.
	 */
	boolean stateExists();
}
