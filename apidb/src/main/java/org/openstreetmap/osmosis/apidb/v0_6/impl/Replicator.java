// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


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
	 * This is the maximum number of transaction ids sent in a single query. If
	 * larger than 2 power 16 it fails due to a 16 bit number failing, but still
	 * fails below that with a stack limit being exceeded. The current value is
	 * near to the maximum value known to work, it will work slightly higher but
	 * this is a round number. It is dependent on the max_stack_depth parameter
	 * defined in postgresql.conf.
	 */
	private static final int TRANSACTION_QUERY_SIZE_MAX = 25000;

	private ChangeSink changeSink;
	private ReplicationSource source;
	private TransactionManager txnManager;
	private SystemTimeLoader systemTimeLoader;
	private int iterations;
	private int minInterval;
	private int maxInterval;


	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The source for all replication changes.
	 * @param changeSink
	 *            The destination for all replicated changes.
	 * @param snapshotLoader
	 *            Loads transaction snapshots from the database.
	 * @param systemTimeLoader
	 *            Loads the current system time from the database.
	 * @param iterations
	 *            The number of replication intervals to execute. 0 means
	 *            infinite.
	 * @param minInterval
	 *            The minimum number of milliseconds between intervals.
	 * @param maxInterval
	 *            The maximum number of milliseconds between intervals if no new
	 *            data is available. This isn't a hard limit because processing
	 *            latency may increase the duration.
	 */
	public Replicator(ReplicationSource source, ChangeSink changeSink,
			TransactionManager snapshotLoader, SystemTimeLoader systemTimeLoader, int iterations,
			int minInterval, int maxInterval) {
		this.source = source;
		this.changeSink = changeSink;
		this.txnManager = snapshotLoader;
		this.systemTimeLoader = systemTimeLoader;
		this.iterations = iterations;
		this.minInterval = minInterval;
		this.maxInterval = maxInterval;
	}


	/**
	 * Obtains a new transaction shapshot from the database and updates the
	 * state to match.
	 * 
	 * @param state
	 *            The replication state.
	 */
	private void obtainNewSnapshot(ReplicationState state) {
		TransactionSnapshot transactionSnapshot;

		// Obtain the latest transaction snapshot from the database.
		transactionSnapshot = txnManager.getTransactionSnapshot();

		// Update the xmax value.
		state.setTxnMax(transactionSnapshot.getXMax());

		// Any items in the old active transaction list but not in the new
		// active transaction list must be added to the ready list.
		for (Iterator<Long> i = state.getTxnActive().iterator(); i.hasNext();) {
			Long id;

			id = i.next();

			if (!transactionSnapshot.getXIpList().contains(id)) {
				// They only need to be added if the maximum queried xmax has
				// been passed.
				if (compareTxnIds(id, state.getTxnMaxQueried()) < 0) {
					state.getTxnReady().add(id);
				}
			}
		}

		// The active transaction list must be updated to match the latest
		// snapshot.
		state.getTxnActive().clear();
		state.getTxnActive().addAll(transactionSnapshot.getXIpList());

		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Updated replication state with new snapshot, maxTxnQueried=" + state.getTxnMaxQueried()
					+ ", maxTxn=" + state.getTxnMax() + ", txnActiveList=" + state.getTxnActive() + ", txnReadyList="
					+ state.getTxnReady() + ".");
		}
	}


	/**
	 * This performs a comparison of the two transaction ids using 32-bit
	 * arithmetic. The result is (id1 - id2), but with both numbers cast to an
	 * integer prior to the comparison. This provides a correct result even in
	 * the case where the transaction id has wrapped around to 0.
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
	 * This adds an offset to the transaction id. It takes into account the
	 * special values 0-2 and adds an extra 3 to the offset accordingly.
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

		// The top transaction id of the next query is the current xMax up to a
		// maximum of
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

		// The state object will only contain ready ids for transaction ranges
		// that have passed already so we must add all of them to the predicate
		// so they get included in this query.
		predicates.getReadyList().addAll(state.getTxnReady());

		// The ready list can be cleared on the state object now.
		state.getTxnReady().clear();

		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Query predicates updated, bottomXid=" + predicates.getBottomTransactionId() + ", topXid="
					+ predicates.getTopTransactionId() + ", activeXidList=" + predicates.getActiveList()
					+ ", readyXidList=" + predicates.getReadyList() + ".");
		}

		return predicates;
	}


	private void copyChanges(ReleasableIterator<ChangeContainer> sourceIterator, ReplicationState state) {
		try {
			Date currentTimestamp;

			// As we process, we must update the timestamp to match the latest
			// record we have received.
			currentTimestamp = state.getTimestamp();

			while (sourceIterator.hasNext()) {
				ChangeContainer change;
				Date nextTimestamp;

				change = sourceIterator.next();
				nextTimestamp = change.getEntityContainer().getEntity().getTimestamp();

				if (currentTimestamp.compareTo(nextTimestamp) < 0) {
					currentTimestamp = nextTimestamp;
				}

				changeSink.process(change);
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
		try {
			replicateLoop();

		} finally {
			changeSink.release();
		}
	}


	/**
	 * The main replication loop. This continues until the maximum number of
	 * replication intervals has been reached.
	 */
	private void replicateLoop() {
		// Perform replication up to the number of iterations, or infinitely if
		// set to 0.
		for (int iterationCount = 1; true; iterationCount++) {

			// Perform the replication interval.
			txnManager.executeWithinTransaction(new Runnable() {
				@Override
				public void run() {
					replicateImpl();
				}
			});
			
			// Stop if we've reached the target number of iterations.
			if (iterations > 0 && iterationCount >= iterations) {
				LOG.fine("Exiting replication loop.");
				break;
			}
		}
	}


	/**
	 * Replicates the next set of changes from the database.
	 */
	private void replicateImpl() {
		ReplicationState state;
		ReplicationQueryPredicates predicates;
		Date systemTimestamp;
		Map<String, Object> metaData;
		
		// Create an initial replication state.
		state = new ReplicationState();
		
		// Initialise the sink and provide it with a reference to the state
		// object. A single state instance is shared by both ends of the
		// pipeline.
		metaData = new HashMap<String, Object>(1);
		metaData.put(ReplicationState.META_DATA_KEY, state);
		changeSink.initialize(metaData);
		
		// Wait until the minimum delay interval has been reached.
		while (true) {
			/*
			 * Determine the time of processing. Note that we must do this after
			 * obtaining the database transaction snapshot. A key rule in
			 * replication is that the timestamp we specify in our replication state
			 * is always equal to or later than all data timestamps. This allows
			 * consumers to know that when they pick a timestamp to start
			 * replicating from that *all* data created after that timestamp will be
			 * included in subsequent replications.
			 */
			systemTimestamp = systemTimeLoader.getSystemTime();
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loaded system time " + systemTimestamp + " from the database.");
			}
			
			// Continue onto next step if we've reached the minimum interval or
			// if our remaining interval exceeds the maximum (clock skew).
			long remainingInterval = state.getTimestamp().getTime() + minInterval - systemTimestamp.getTime();
			if (remainingInterval <= 0 || remainingInterval > minInterval) {
				break;
			} else {
				try {
					Thread.sleep(remainingInterval);
				} catch (InterruptedException e) {
					throw new OsmosisRuntimeException("Unable to sleep until next replication iteration.", e);
				}
			}
		}

		// Wait until either data becomes available or the maximum interval is reached.
		while (true) {
			// Update our view of the current database transaction state.
			obtainNewSnapshot(state);
			
			// Continue onto next step if data is available.
			if (state.getTxnMaxQueried() != state.getTxnMax() || state.getTxnReady().size() > 0) {
				break;
			}
			
			systemTimestamp = systemTimeLoader.getSystemTime();
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loaded system time " + systemTimestamp + " from the database.");
			}
			
			// Continue onto next step if we've reached the maximum interval or
			// if our remaining interval exceeds the maximum (clock skew).
			long remainingInterval = state.getTimestamp().getTime() + maxInterval - systemTimestamp.getTime();
			if (remainingInterval <= 0 || remainingInterval > maxInterval) {
				break;
			} else {
				long sleepInterval = remainingInterval;
				if (sleepInterval > minInterval) {
					sleepInterval = minInterval;
				}
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {
					throw new OsmosisRuntimeException("Unable to sleep until data becomes available.", e);
				}
			}
		}

		LOG.fine("Processing replication sequence.");
		
		/*
		 * We must get the latest timestamp before proceeding. Using an earlier
		 * timestamp runs the risk of marking a replication sequence with a
		 * timestamp that is too early which may lead to replication clients
		 * starting with a later sequence than they should.
		 */
		systemTimestamp = systemTimeLoader.getSystemTime();
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Loaded system time " + systemTimestamp + " from the database.");
		}
		
		// If this is the first interval we are setting an initial state but not
		// performing any replication.
		if (state.getSequenceNumber() == 0) {
			// We are starting from the current point so we are at the current
			// database timestamp and transaction id.
			state.setTimestamp(systemTimestamp);
			state.setTxnMaxQueried(state.getTxnMax());
			
		} else {
			// Obtain the predicates to use during the query.
			predicates = buildQueryPredicates(state);
	
			// Write the changes to the destination.
			if (predicates.getBottomTransactionId() != predicates.getTopTransactionId()) {
				copyChanges(source.getHistory(predicates), state);
			}
	
			/*
			 * If we have completely caught up to the database, we update the
			 * timestamp to the database system timestamp. Otherwise we leave
			 * the timestamp set at the value determined while processing
			 * changes. We update to the system timestamp when caught up to
			 * ensure that a current timestamp is provided to consumers in the
			 * case where no data has been created.
			 */
			if (compareTxnIds(state.getTxnMaxQueried(), state.getTxnMax()) >= 0) {
				state.setTimestamp(systemTimestamp);
			}
		}

		// Commit changes.
		changeSink.complete();
		
		LOG.fine("Replication sequence complete.");
	}
}
