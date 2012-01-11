// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.pipeline.common.TaskRunner;
import org.openstreetmap.osmosis.replication.common.ReplicationSequenceFormatter;
import org.openstreetmap.osmosis.replication.v0_6.ReplicationWriter;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Performs an end to end test of the HTTP replication classes.
 * 
 * @author Brett Henderson
 */
public class ReplicationTest extends AbstractDataTest {

	/**
	 * Configures logging to write all output to the console.
	 */
	private static void configureLoggingConsole() {
		Logger rootLogger;
		Handler consoleHandler;

		rootLogger = Logger.getLogger("");

		// Remove any existing handlers.
		for (Handler handler : rootLogger.getHandlers()) {
			rootLogger.removeHandler(handler);
		}

		// Add a new console handler.
		consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		rootLogger.addHandler(consoleHandler);
	}


	/**
	 * Configures the logging level.
	 * 
	 * @param level
	 *            The new logging level to apply.
	 */
	private static void configureLoggingLevel(Level level) {
		Logger rootLogger;

		rootLogger = Logger.getLogger("");

		// Set the required logging level.
		rootLogger.setLevel(level);

		// Set the JPF logger to one level lower.
		Logger.getLogger("org.java.plugin").setLevel(Level.WARNING);
	}


	/**
	 * Single end to end test.
	 * 
	 * @throws Exception
	 *             if an error occurs during processing.
	 */
	@Test
	public void test() throws Exception {
		final int sequenceCount = 100;
		long timerStart;

		// Due to the multi-threaded nature of this test, it may be necessary to
		// enable logging to diagnose problems.
		final boolean enableLogging = false;
		if (enableLogging) {
			configureLoggingConsole();
			configureLoggingLevel(Level.FINEST);
			Logger.getLogger("org.openstreetmap.osmosis.replication.v0_6.ReplicationStateWriter").setLevel(Level.INFO);
		}

		// Create the primary replication data source.
		MockReplicationSource source = new MockReplicationSource();

		// Create the sequence server for notifying when new sequence numbers
		// are available and connect it to the primary source.
		ReplicationSequenceServer sequenceServer = new ReplicationSequenceServer(0);
		source.setChangeSink(sequenceServer);

		// Create a replication data writer and receive data from the primary
		// data source (via the sequence server).
		File workingDir1 = dataUtils.newFolder();
		sequenceServer.setChangeSink(new ReplicationWriter(workingDir1));

		// Send sequence through the primary pipeline to ensure the
		// sequence server is running.
		source.sendSequence();

		// Create a HTTP replication data server using the data from the
		// replication writer, and receive sequence number updates from the
		// sequence server.
		ReplicationDataServer dataServer = new ReplicationDataServer(sequenceServer.getPort(), workingDir1, 0);

		// Start the HTTP data server.
		TaskRunner serverRunner = new TaskRunner(dataServer, "data-server");
		serverRunner.start();

		/*
		 * The server starts in another thread so we need to wait until it has
		 * started. We will wait until the dynamically allocated port is
		 * exported via the getPort method which occurs after server startup.
		 */
		timerStart = System.currentTimeMillis();
		while (dataServer.getPort() == 0 && (System.currentTimeMillis() - timerStart < 10000)) {
			Thread.sleep(10);
		}
		Assert.assertFalse("Server port was not dynamically allocated.", sequenceServer.getPort() == 0);

		// Create a HTTP replication data client receiving data from the data
		// server.
		ReplicationDataClient dataClient = new ReplicationDataClient(new InetSocketAddress(dataServer.getPort()), "");

		// Create a replication data writer to receiving data from the HTTP data
		// source.
		File workingDir2 = dataUtils.newFolder();
		dataClient.setChangeSink(new ReplicationWriter(workingDir2));

		// Start the HTTP data server and HTTP data client.
		TaskRunner clientRunner = new TaskRunner(dataClient, "data-client");
		clientRunner.start();

		// Send the test replication intervals.
		for (int i = 0; i < sequenceCount; i++) {
			source.sendSequence();
		}

		// Wait for all the data to reach the destination.
		File finalStateFile = new File(workingDir2, new ReplicationSequenceFormatter(9, 3).getFormattedName(
				sequenceCount, ".state.txt"));
		timerStart = System.currentTimeMillis();
		while (!finalStateFile.exists() && (System.currentTimeMillis() - timerStart < 10000)) {
			Thread.sleep(100);
		}

		// Verify that all of the replication sequences made it to the
		// destination.
		Assert.assertTrue("The state file for sequence " + sequenceCount + " doesn't exist.", finalStateFile.exists());

		// Shut down the pipelines.
		clientRunner.interrupt();
		serverRunner.interrupt();
		clientRunner.join();
		serverRunner.join();
		source.release();
	}
}
