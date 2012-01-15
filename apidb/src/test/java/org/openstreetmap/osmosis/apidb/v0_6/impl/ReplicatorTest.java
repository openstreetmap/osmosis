// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Tests for the Replicator class.
 */
public class ReplicatorTest {

	private Date buildDate(String rawDate) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rawDate);
		} catch (ParseException e) {
			throw new OsmosisRuntimeException("The date could not be parsed.", e);
		}
	}


	/**
	 * Tests replication behaviour during initialisation. Initialisation occurs the first time
	 * replication is run.
	 */
	@Test
	public void testInitialization() {
		Replicator replicator;
		MockReplicationSource source;
		MockReplicationDestination destination;
		MockTransactionSnapshotLoader snapshotLoader;
		MockSystemTimeLoader timeLoader;
		ReplicationState state;
		
		// Build the mocks.
		source = new MockReplicationSource();
		destination = new MockReplicationDestination();
		snapshotLoader = new MockTransactionSnapshotLoader();
		timeLoader = new MockSystemTimeLoader();
		
		// Instantiate the new replicator.
		replicator = new Replicator(source, destination, snapshotLoader, timeLoader, 1, 0, 0);
		
		// Provide initialisation data.
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:14"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:14"));
		snapshotLoader.getSnapshots().add(new TransactionSnapshot("100:200:110,112"));
		
		// Launch the replication process.
		replicator.replicate();
		
		// Verify the final state.
		state = destination.getCurrentState();
		Assert.assertEquals("Incorrect final state.",
				new ReplicationState(
						200,
						200,
						Arrays.asList(new Long[]{110L, 112L}),
						Arrays.asList(new Long[]{}),
						buildDate("2009-10-11 12:13:14"),
						0),
				state);
	}


	/**
	 * Tests replication behaviour when no replication is required.
	 */
	@Test
	public void testNoAction() {
		Replicator replicator;
		MockReplicationSource source;
		MockReplicationDestination destination;
		MockTransactionSnapshotLoader snapshotLoader;
		MockSystemTimeLoader timeLoader;
		ReplicationState initialState;
		ReplicationState finalState;
		
		// Build initial replication state.
		initialState = new ReplicationState(
				200,
				200,
				Arrays.asList(new Long[]{110L, 112L}),
				Arrays.asList(new Long[]{}),
				buildDate("2009-10-11 12:13:14"),
				0);
		
		// Build the mocks.
		source = new MockReplicationSource();
		destination = new MockReplicationDestination(initialState);
		snapshotLoader = new MockTransactionSnapshotLoader();
		timeLoader = new MockSystemTimeLoader();
		
		// Instantiate the new replicator.
		replicator = new Replicator(source, destination, snapshotLoader, timeLoader, 1, 0, 0);
		
		// We want the snapshot loader to return the same snapshot to simulate no database changes.
		snapshotLoader.getSnapshots().add(new TransactionSnapshot("100:200:110,112"));
		// But we want the clock time to have progressed.
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		
		// Launch the replication process.
		replicator.replicate();
		
		// Verify that the final state does not match the initial state, but that the only
		// difference is the time and increment sequence number.
		finalState = destination.getCurrentState();
		Assert.assertFalse("Final state should not match initial state.", finalState.equals(initialState));
		finalState.setTimestamp(initialState.getTimestamp());
		finalState.setSequenceNumber(finalState.getSequenceNumber() - 1);
		Assert.assertTrue("Final state should match initial state after updating timestamp.",
				finalState.equals(initialState));
		
		// Verify that no changes were replicated.
		Assert.assertTrue("No changes should have been replicated.", source.getPredicatesList().size() == 0);
	}


	/**
	 * Tests replication behaviour when a simple replication interval is required.
	 */
	@Test
	public void testSimpleIncrement() {
		Replicator replicator;
		MockReplicationSource source;
		MockReplicationDestination destination;
		MockTransactionSnapshotLoader snapshotLoader;
		MockSystemTimeLoader timeLoader;
		ReplicationState state;
		ReplicationQueryPredicates predicates;
		
		// Build initial replication state.
		state = new ReplicationState(
				200,
				200,
				Arrays.asList(new Long[]{}),
				Arrays.asList(new Long[]{}),
				buildDate("2009-10-11 12:13:14"),
				0);
		
		// Build the mocks.
		source = new MockReplicationSource();
		destination = new MockReplicationDestination(state);
		snapshotLoader = new MockTransactionSnapshotLoader();
		timeLoader = new MockSystemTimeLoader();
		
		// Instantiate the new replicator.
		replicator = new Replicator(source, destination, snapshotLoader, timeLoader, 1, 0, 0);
		
		// Set the snapshot loader to return a snapshot with higher xMax.
		snapshotLoader.getSnapshots().add(new TransactionSnapshot("100:220"));
		// We also want the clock time to have progressed.
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		
		// Launch the replication process.
		replicator.replicate();
		
		// Verify that the final state is correct.
		state = destination.getCurrentState();
		Assert.assertEquals("Incorrect final state.",
				new ReplicationState(
						220,
						220,
						Arrays.asList(new Long[]{}),
						Arrays.asList(new Long[]{}),
						buildDate("2009-10-11 12:13:15"),
						1),
				state);
		
		// Verify that the correct changes were replicated.
		Assert.assertTrue("A single interval should have been replicated.", source.getPredicatesList().size() == 1);
		predicates = source.getPredicatesList().get(0);
		Assert.assertEquals("Incorrect active list.", Collections.emptyList(), predicates.getActiveList());
		Assert.assertEquals("Incorrect ready list.", Collections.emptyList(), predicates.getReadyList());
		Assert.assertEquals("Incorrect bottom transaction id.", 200, predicates.getBottomTransactionId());
		Assert.assertEquals("Incorrect top transaction id.", 220, predicates.getTopTransactionId());
	}


	/**
	 * Tests replication behaviour when active list manipulation is required.
	 */
	@Test
	public void testInFlightTxnIncrement() {
		Replicator replicator;
		MockReplicationSource source;
		MockReplicationDestination destination;
		MockTransactionSnapshotLoader snapshotLoader;
		MockSystemTimeLoader timeLoader;
		ReplicationState state;
		ReplicationQueryPredicates predicates;
		
		// Build initial replication state.
		state = new ReplicationState(
				200,
				200,
				Arrays.asList(new Long[]{180L, 185L}),
				Arrays.asList(new Long[]{}),
				buildDate("2009-10-11 12:13:14"),
				0);
		
		// Build the mocks.
		source = new MockReplicationSource();
		destination = new MockReplicationDestination(state);
		snapshotLoader = new MockTransactionSnapshotLoader();
		timeLoader = new MockSystemTimeLoader();
		
		// Instantiate the new replicator.
		replicator = new Replicator(source, destination, snapshotLoader, timeLoader, 1, 0, 0);
		
		// Set the snapshot loader to return a snapshot with higher xMax.
		snapshotLoader.getSnapshots().add(new TransactionSnapshot("100:220:185"));
		// We also want the clock time to have progressed.
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		
		// Launch the replication process.
		replicator.replicate();
		
		// Verify that the final state is correct.
		state = destination.getCurrentState();
		Assert.assertEquals("Incorrect final state.",
				new ReplicationState(
						220,
						220,
						Arrays.asList(new Long[]{185L}),
						Arrays.asList(new Long[]{}),
						buildDate("2009-10-11 12:13:15"),
						1),
				state);
		
		// Verify that the correct changes were replicated.
		Assert.assertTrue("A single interval should have been replicated.", source.getPredicatesList().size() == 1);
		predicates = source.getPredicatesList().get(0);
		Assert.assertEquals("Incorrect active list.", Arrays.asList(new Long[]{185L}), predicates.getActiveList());
		Assert.assertEquals("Incorrect ready list.", Arrays.asList(new Long[]{180L}), predicates.getReadyList());
		Assert.assertEquals("Incorrect bottom transaction id.", 200, predicates.getBottomTransactionId());
		Assert.assertEquals("Incorrect top transaction id.", 220, predicates.getTopTransactionId());
	}


	/**
	 * Tests replication behaviour when catching up from outage and some active transactions are overtaken.
	 */
	@Test
	public void testOutageCatchupWithActiveTxns() {
		Replicator replicator;
		MockReplicationSource source;
		MockReplicationDestination destination;
		MockTransactionSnapshotLoader snapshotLoader;
		MockSystemTimeLoader timeLoader;
		ReplicationState state;
		ReplicationQueryPredicates predicates;
		
		// Build initial replication state.
		state = new ReplicationState(
				5,
				5,
				Arrays.asList(new Long[]{24000L, 26000L}),
				Arrays.asList(new Long[]{}),
				buildDate("2009-10-11 12:13:14"),
				0);
		
		// Build the mocks.
		source = new MockReplicationSource();
		destination = new MockReplicationDestination(state);
		snapshotLoader = new MockTransactionSnapshotLoader();
		timeLoader = new MockSystemTimeLoader();
		
		// Instantiate the new replicator.
		replicator = new Replicator(source, destination, snapshotLoader, timeLoader, 1, 0, 0);
		
		// Set the snapshot loader to return a snapshot with higher xMax.
		snapshotLoader.getSnapshots().add(new TransactionSnapshot("20000:30000:26000"));
		// We also want the clock time to have progressed.
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		timeLoader.getTimes().add(buildDate("2009-10-11 12:13:15"));
		
		// Launch the replication process.
		replicator.replicate();
		
		// Verify that the final state is correct.
		state = destination.getCurrentState();
		Assert.assertEquals("Incorrect final state.",
				new ReplicationState(
						30000,
						25005,
						Arrays.asList(new Long[]{26000L}),
						Arrays.asList(new Long[]{}),
						buildDate("2009-10-11 12:13:14"),
						1),
				state);
		
		// Verify that the correct changes were replicated.
		Assert.assertTrue("A single interval should have been replicated.", source.getPredicatesList().size() == 1);
		predicates = source.getPredicatesList().get(0);
		Assert.assertEquals("Incorrect active list.", Arrays.asList(new Long[]{26000L}), predicates.getActiveList());
		Assert.assertEquals("Incorrect ready list.", Arrays.asList(new Long[]{}), predicates.getReadyList());
		Assert.assertEquals("Incorrect bottom transaction id.", 5, predicates.getBottomTransactionId());
		Assert.assertEquals("Incorrect top transaction id.", 25005, predicates.getTopTransactionId());
	}
}
