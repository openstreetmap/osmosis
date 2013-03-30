// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * A mocked replication destination allowing the existing replication state to be loaded and the
 * current state maintained. All data processing calls such as process will be ignored.
 */
public class MockReplicationDestination implements ChangeSink {
	
	private boolean stateExists;
	private ReplicationState currentState;
	private Map<String, String> storedState;
	
	
	/**
	 * Creates a new instance with no state.
	 */
	public MockReplicationDestination() {
		stateExists = false;
		storedState = new HashMap<String, String>();
	}
	
	
	/**
	 * Creates a new instance with an initial state.
	 * 
	 * @param initialState
	 *            The initial replication state.
	 */
	public MockReplicationDestination(ReplicationState initialState) {
		this();
		
		initialState.store(storedState);
		stateExists = true;
	}


    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		// Get the replication state from the upstream task.
		if (!metaData.containsKey(ReplicationState.META_DATA_KEY)) {
			throw new OsmosisRuntimeException(
					"No replication state has been provided in metadata key " + ReplicationState.META_DATA_KEY + ".");
		}
		currentState = (ReplicationState) metaData.get(ReplicationState.META_DATA_KEY);
		
		// Initialise the state from the stored state if it exists and increment
		// the sequence number.
		if (stateExists) {
			currentState.load(storedState);
			currentState.setSequenceNumber(currentState.getSequenceNumber() + 1);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		currentState.store(storedState);
		stateExists = true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
	
	
	/**
	 * Returns the current state object tracked internally. This will be a state
	 * object provided by a caller in the initialize method. It will remain
	 * available after complete and release have been called.
	 * 
	 * @return The current state.
	 */
	public ReplicationState getCurrentState() {
		return currentState;
	}
}
