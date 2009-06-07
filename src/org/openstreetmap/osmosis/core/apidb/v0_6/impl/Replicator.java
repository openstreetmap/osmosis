// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
	/**
	 * This is the maximum number of transaction ids sent in a single query. If larger than 2 power
	 * 16 it fails due to a 16 bit number failing, but still fails below that with a stack limit
	 * being exceeded. The current value is near to the maximum value known to work, it will work
	 * slightly higher but this is a round number.
	 */
	private static final int TRANSACTION_QUERY_SIZE_MAX = 25000;
	/**
	 * When querying, the data will be constrained to data within a period of this length in
	 * milliseconds. This is because the transaction id columns in the database are not indexed.
	 */
	private static final long BASE_TIMESTAMP_OFFSET = 1000 * 1000 * 60;
	
	private ReplicationDestination destination;
	private ReplicationSource source;
	private TransactionSnapshotLoader snapshotLoader;
	private SystemTimeLoader systemTimeLoader;


	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The source for all replication changes.
	 * @param destination
	 *            The destination for all replicated changes.
	 * @param snapshotLoader
	 *            Loads transaction snapshots from the database.
	 * @param systemTimeLoader
	 *            Loads the current system time from the database.
	 */
	public Replicator(ReplicationSource source, ReplicationDestination destination,
			TransactionSnapshotLoader snapshotLoader, SystemTimeLoader systemTimeLoader) {
		this.source = source;
		this.destination = destination;
		this.snapshotLoader = snapshotLoader;
		this.systemTimeLoader = systemTimeLoader;
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
			if (compareTxnIds(currentId, state.getTxnMax()) >= 0) {
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
	
	
	private void copyChanges(ReleasableIterator<ChangeContainer> sourceIterator, ReplicationState state) {
		try {
			Date currentTimestamp;
			
			// As we process, we must update the timestamp to match the latest record we have
			// received.
			currentTimestamp = state.getTimestamp();
			
			while (sourceIterator.hasNext()) {
				ChangeContainer change;
				Date nextTimestamp;
				
				change = sourceIterator.next();
				nextTimestamp = change.getEntityContainer().getEntity().getTimestamp();
				
				if (currentTimestamp.compareTo(nextTimestamp) < 0) {
					currentTimestamp = nextTimestamp;
				}
				
				destination.process(change);
			}
			
			state.setTimestamp(currentTimestamp);
			
		} finally {
			sourceIterator.release();
		}
	}
	
	
	/**
	 * Replicates the next set of changes from the database.
	 */
	public void replicate() {
		// If we have already run once we begin replication, otherwise we initialise to the current
		// database state.
		if (destination.stateExists()) {
			replicateImpl();
		} else {
			initialize();
		}
	}
	
	
	/**
	 * Replicates the next set of changes from the database.
	 */
	private void replicateImpl() {
		try {
			ReplicationState state;
			List<Long> transactionQueryList;
			Date systemTimestamp;
			Date baseTimestamp;
			
			// Determine the time of processing.
			systemTimestamp = systemTimeLoader.getSystemTime();
			
			// Load the current replication state.
			state = destination.loadState();
			
			// Calculate the base timestamp. The transaction ids are not indexed in the database,
			// therefore we need to use a timestamp range that will contain all values but not
			// overload the database too much by retrieving too many records.
			baseTimestamp = new Date(state.getTimestamp().getTime() - BASE_TIMESTAMP_OFFSET);
			
			// Increment the current replication sequence number.
			state.setSequenceNumber(state.getSequenceNumber() + 1);
			
			// If the maximum queried transaction id has reached the maximum transaction id then a new
			// transaction snapshot must be obtained in order to get more data.
			if (compareTxnIds(state.getTxnMaxQueried(), state.getTxnMax()) >= 0) {
				obtainNewSnapshot(state);
			}
			
			// Obtain the transaction list to use during the query.
			transactionQueryList = buildTransactionQueryList(state);
			
			// Write the changes to the destination.
			if (transactionQueryList.size() > 0) {
				copyChanges(source.getHistory(baseTimestamp, transactionQueryList), state);
			}
			
			// If we have completely caught up to the database, we can update the timestamp to the
			// database system timestamp. Otherwise we leave the timestamp set at the value
			// determined while processing changes.
			if (compareTxnIds(state.getTxnMaxQueried(), state.getTxnMax()) >= 0) {
				state.setTimestamp(systemTimestamp);
			}
			
			// Persist the updated replication state.
			destination.saveState(state);
			
			// Commit changes.
			destination.complete();
			
		} finally {
			destination.release();
		}
	}
	
	
	/**
	 * Initialises the destination using the current state of the source.
	 */
	private void initialize() {
		try {
			TransactionSnapshot snapshot;
			ReplicationState state;
			Date systemTime;
			
			snapshot = snapshotLoader.getTransactionSnapshot();
			systemTime = systemTimeLoader.getSystemTime();
			
			// Create a new state initialised with the current state of the database.
			state = new ReplicationState(snapshot.getXMax(), snapshot.getXMax(), new ArrayList<Long>(),
					new ArrayList<Long>(), systemTime, 0);
			
			// Persist the updated replication state.
			destination.saveState(state);
			
			// Commit changes.
			destination.complete();
			
		} finally {
			destination.release();
		}
	}
}
