// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Replicates changes from the database utilising transaction snapshots.
 */
public class Replicator {
	
	private static final Logger LOG = Logger.getLogger(Replicator.class.getName());
	
	
	/**
	 * The number of special transactions beginning from 0.
	 */
	private static final int SPECIAL_TRANSACTION_OFFSET = 3;
	/**
	 * This is the maximum number of transaction ids sent in a single query. If larger than 2 power
	 * 16 it fails due to a 16 bit number failing, but still fails below that with a stack limit
	 * being exceeded. The current value is near to the maximum value known to work, it will work
	 * slightly higher but this is a round number. It is dependent on the max_stack_depth parameter
	 * defined in postgresql.conf.
	 */
	private static final int TRANSACTION_QUERY_SIZE_MAX = 25000;
	
	
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
	
	
	/**
	 * Obtains a new transaction shapshot from the database and updates the state to match.
	 * 
	 * @param state
	 *            The replication state.
	 */
	private void obtainNewSnapshot(ReplicationState state) {
		TransactionSnapshot transactionSnapshot;
		
		// Obtain the latest transaction snapshot from the database.
		transactionSnapshot = snapshotLoader.getTransactionSnapshot();
		
		// Update the xmax value.
		state.setTxnMax(transactionSnapshot.getXMax());
		
		// Any items in the old active transaction list but not in the new active transaction list
		// must be added to the ready list.
		for (Iterator<Long> i = state.getTxnActive().iterator(); i.hasNext();) {
			Long id;
			
			id = i.next();
			
			if (!transactionSnapshot.getXIpList().contains(id)) {
				// They only need to be added if the maximum queried xmax has been passed.
				if (compareTxnIds(id, state.getTxnMaxQueried()) <= 0) {
					state.getTxnReady().add(id);
				}
			}
		}
		
		// The active transaction list must be updated to match the latest snapshot.
		state.getTxnActive().clear();
		state.getTxnActive().addAll(transactionSnapshot.getXIpList());
		
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Updated replication state with new snapshot, maxTxnQueried="
					+ state.getTxnMaxQueried() + ", maxTxn=" + state.getTxnMax()
					+ ", txnActiveList=" + state.getTxnActive()
					+ ", txnReadyList=" + state.getTxnReady() + ".");
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
	 * @return The difference between the two transaction ids (id1 - id2).
	 */
	private int compareTxnIds(long id1, long id2) {
		return ((int) id1) - ((int) id2);
	}
	
	
	/**
	 * This adds an offset to the transaction id. It takes into account the special values 0-2 and
	 * adds an extra 3 to the offset accordingly.
	 * 
	 * @param id
	 *            The transaction id.
	 * @param increment
	 *            The amount to increment the id.
	 * @return The result transaction id.
	 */
	private long incrementTxnId(long id, int increment) {
		int oldId;
		int newId;
		
		oldId = (int) id;
		newId = oldId + increment;
		
		if (oldId < 0 && newId >= 0) {
			newId += SPECIAL_TRANSACTION_OFFSET;
		}
		
		return newId;
	}
	
	
	private ReplicationQueryPredicates buildQueryPredicates(ReplicationState state) {
		long topTransactionId;
		int rangeLength;
		ReplicationQueryPredicates predicates;
		
		// The top transaction id of the next query is the current xMax up to a maximum of
		// TRANSACTION_QUERY_SIZE_MAX transactions.
		topTransactionId = state.getTxnMax();
		rangeLength = compareTxnIds(topTransactionId, state.getTxnMaxQueried());
		if (rangeLength > TRANSACTION_QUERY_SIZE_MAX) {
			topTransactionId = incrementTxnId(state.getTxnMaxQueried(), TRANSACTION_QUERY_SIZE_MAX);
		}
		
		// Build the predicate object with the new range.
		predicates = new ReplicationQueryPredicates(state.getTxnMaxQueried(), topTransactionId);
		
		// Update the state with the new queried marker.
		state.setTxnMaxQueried(topTransactionId);
		
		// Copy the active transaction list into the predicate.
		predicates.getActiveList().addAll(state.getTxnActive());
		
		// The state object will only contain ready ids for transaction ranges that have passed
		// already so we must add all of them to the predicate so they get included in this query.
		predicates.getReadyList().addAll(state.getTxnReady());
		
		// The ready list can be cleared on the state object now.
		state.getTxnReady().clear();
		
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Query predicates updated, bottomXid="
					+ predicates.getBottomTransactionId()
					+ ", topXid=" + predicates.getTopTransactionId()
					+ ", activeXidList=" + predicates.getActiveList()
					+ ", readyXidList=" + predicates.getReadyList() + ".");
		}
		
		return predicates;
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
			LOG.fine("Replication state exists, beginning replication.");
			replicateImpl();
		} else {
			LOG.fine("Replication state does not exist, initializing.");
			initialize();
		}
	}
	
	
	/**
	 * Replicates the next set of changes from the database.
	 */
	private void replicateImpl() {
		try {
			ReplicationState state;
			ReplicationQueryPredicates predicates;
			Date systemTimestamp;
			
			// Determine the time of processing.
			systemTimestamp = systemTimeLoader.getSystemTime();
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loaded system time " + systemTimestamp + " from the database.");
			}
			
			// Load the current replication state.
			state = destination.loadState();
			
			// Increment the current replication sequence number.
			state.setSequenceNumber(state.getSequenceNumber() + 1);
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Replication sequence number is " + state.getSequenceNumber() + ".");
			}
			
			// If the maximum queried transaction id has reached the maximum transaction id then a new
			// transaction snapshot must be obtained in order to get more data.
			if (compareTxnIds(state.getTxnMaxQueried(), state.getTxnMax()) >= 0) {
				obtainNewSnapshot(state);
			}
			
			// Obtain the predicates to use during the query.
			predicates = buildQueryPredicates(state);
			
			// Write the changes to the destination.
			if (predicates.getBottomTransactionId() != predicates.getTopTransactionId()) {
				copyChanges(source.getHistory(predicates), state);
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
