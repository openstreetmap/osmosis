// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.common;

import org.openstreetmap.osmosis.xml.v0_6.XmlChangeReader;
import org.openstreetmap.osmosis.xml.v0_6.XmlChangeWriter;


/**
 * Defines a store for replication data.
 * 
 * @author Brett Henderson
 */
public interface ReplicationStore {
	/**
	 * Gets the current replication state. This corresponds to the state of the
	 * latest sequence in the store.
	 * 
	 * @return The replication state.
	 */
	ReplicationState getCurrentState();


	/**
	 * Gets the state for the specified sequence.
	 * 
	 * @param sequence
	 *            The sequence to be loaded.
	 * @return The replication state.
	 */
	ReplicationState getState(long sequence);


	/**
	 * Gets the data for the specified sequence.
	 * 
	 * @param sequence
	 *            The sequence to be loaded.
	 * @return The change reader.
	 */
	XmlChangeReader getData(long sequence);


	/**
	 * Sets the state for the specified sequence. The current state may be
	 * updated to match depending on the store configuration. This should only
	 * be called after data has been successfully written.
	 * 
	 * @param state
	 *            The replication state.
	 */
	void saveState(ReplicationState state);


	/**
	 * Obtains an change writer used to save the replication data for the
	 * specified sequence.
	 * 
	 * @param sequence
	 *            The sequence to be saved.
	 * @return The change writer.
	 */
	XmlChangeWriter saveData(long sequence);
}
