// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Replicates changes from the database utilising transaction snapshots.
 */
public class Replicator {
	private static final long TRANSACTION_ID_MAX = 4294967295L;
	private static final long TRANSACTION_ID_MIN = 3L;
	private static final int TRANSACTION_QUERY_SIZE_MAX = 100000;
	
	private ReplicationDestination destination;
	private ReplicationSource source;
	private TransactionSnapshotLoader snapshotLoader;


	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The source for all replication changes.
	 * @param destination
	 *            The destination for all replicated changes.
	 * @param snapshotLoader
	 *            Loads transaction snapshots from the database.
	 */
	public Replicator(ReplicationSource source, ReplicationDestination destination,
			TransactionSnapshotLoader snapshotLoader) {
		this.source = source;
		this.destination = destination;
		this.snapshotLoader = snapshotLoader;
	}
	
	
	private void obtainNewSnapshot(ReplicationState state) {
		TransactionSnapshot transactionSnapshot;
		
		// Obtain the latest transaction snapshot from the database.
		transactionSnapshot = snapshotLoader.getTransactionSnapshot();
		
		// Update the xmax value.
		state.setTxnMax(transactionSnapshot.getXMax());
		
		// Any items in the old active transaction list but not in the new active transaction list
		// must be moved to the ready list.
		for (Iterator<Long> i = state.getTxnActive().iterator(); i.hasNext();) {
			Long id;
			
			id = i.next();
			
			if (!transactionSnapshot.getXIpList().contains(id)) {
				// They only need to be added if the maximum queried xmax has been passed.
				if (compareTxnIds(id, state.getTxnMaxQueried()) <= 0) {
					state.getTxnReady().add(id);
				}
				
				i.remove();
			}
		}
	}
	
	
	/**
	 * This performs a comparison of the two transaction ids using 32-bit arithmetic. The result is
	 * (id1 - id2), but with both numbers cast to an integer prior to the comparison. This provides
	 * a correct result even in the case where the transaction id has wrapped around to 0.
	 * 
	 * @param id1
	 *            The first transaction id.
	 * @param id2
	 *            The second transaction id.
	 * @return The difference between the two transaction ids.
	 */
	private int compareTxnIds(long id1, long id2) {
		return ((int) id1) - ((int) id2);
	}
	
	
	private List<Long> buildTransactionQueryList(ReplicationState state) {
		ArrayList<Long> transactionQueryList;
		long currentId;
		List<Long> readyList;
		
		// The ready list must be sorted in reverse order because we'll keep removing items from the
		// end of the list.
		readyList = state.getTxnReady();
		Collections.sort(readyList);
		Collections.reverse(readyList);
		
		currentId = state.getTxnMaxQueried();
		
		transactionQueryList = new ArrayList<Long>();
		while (true) {
			if (currentId == state.getTxnMax()) {
				break;
			}
			
			// Move to the next id value.
			currentId++;
			
			// If the transaction id has exceeded the maximum 32-bit unsigned number then wrap back to the start.
			if (currentId > TRANSACTION_ID_MAX) {
				currentId = TRANSACTION_ID_MIN;
			}
			
			// If the current ready list contains this id, remove it.
			if (readyList.contains(currentId)) {
				readyList.remove(currentId);
			}
			// Add the current transaction id to the query list, but only if it isn't currently active.
			if (!state.getTxnActive().contains(currentId)) {
				transactionQueryList.add(currentId);
			}
			
			// If the query has reached the maximum size, then stop adding ids.
			if (transactionQueryList.size() >= TRANSACTION_QUERY_SIZE_MAX) {
				break;
			}
		}
		
		// Mark the xmax that was reached during this invocation.
		state.setTxnMaxQueried(currentId);
		
		return transactionQueryList;
	}
	
	
	private void copyChanges(ReleasableIterator<ChangeContainer> sourceIterator) {
		try {
			while (sourceIterator.hasNext()) {
				destination.process(sourceIterator.next());
			}
			
		} finally {
			sourceIterator.release();
		}
	}
	
	
	/**
	 * Replicates the next set of changes from the database.
	 */
	public void replicate() {
		try {
			ReplicationState state;
			List<Long> transactionQueryList;
			
			// Load the current replication state.
			state = destination.loadState();
			
			// Increment the current replication sequence number.
			state.setSequenceNumber(state.getSequenceNumber() + 1);
			
			// If the maximum queried transaction id has reached the maximum transaction id then a new
			// transaction snapshot must be obtained in order to get more data.
			if (state.getTxnMaxQueried() == state.getTxnMax()) {
				obtainNewSnapshot(state);
			}
			
			// Obtain the transaction list to use during the query.
			transactionQueryList = buildTransactionQueryList(state);
			
			// Write the changes to the destination.
			copyChanges(source.getHistory(state.getTimestamp(), transactionQueryList));
			
			// Persist the updated replication state.
			destination.saveState(state);
			
			// Commit changes.
			destination.complete();
			
		} finally {
			destination.release();
		}
	}
}
