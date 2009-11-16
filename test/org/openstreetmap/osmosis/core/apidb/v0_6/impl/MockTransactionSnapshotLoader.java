// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;


/**
 * A mocked transaction snapshot loader allowing canned snapshots to be returned.
 */
public class MockTransactionSnapshotLoader implements TransactionSnapshotLoader {

	private List<TransactionSnapshot> snapshots = new ArrayList<TransactionSnapshot>();


	/**
	 * Gets the currently available snapshots.
	 * 
	 * @return The snapshots.
	 */
	public List<TransactionSnapshot> getSnapshots() {
		return snapshots;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public TransactionSnapshot getTransactionSnapshot() {
		return snapshots.remove(0);
	}
}
