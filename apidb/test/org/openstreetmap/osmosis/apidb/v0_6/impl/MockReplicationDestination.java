// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;


/**
 * A mocked replication destination allowing the existing replication state to be loaded and the
 * current state maintained. All data processing calls such as process/complete/etc will be ignored.
 */
public class MockReplicationDestination implements ReplicationDestination {
	
	private boolean stateExists;
	private ReplicationState currentState;
	
	
	/**
	 * Creates a new instance with no state.
	 */
	public MockReplicationDestination() {
		stateExists = false;
	}
	
	
	/**
	 * Creates a new instance with an initial state.
	 * 
	 * @param initialState
	 *            The initial replication state.
	 */
	public MockReplicationDestination(ReplicationState initialState) {
		this.currentState = new ReplicationState(
				initialState.getTxnMax(),
				initialState.getTxnMaxQueried(),
				initialState.getTxnActive(),
				initialState.getTxnReady(),
				initialState.getTimestamp(),
				initialState.getSequenceNumber());
		
		stateExists = true;
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReplicationState loadState() {
		if (!stateExists) {
			throw new OsmosisRuntimeException("No state currently exists.");
		}
		
		return currentState;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveState(ReplicationState state) {
		this.currentState = state;
		
		stateExists = true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean stateExists() {
		return stateExists;
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
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
}
