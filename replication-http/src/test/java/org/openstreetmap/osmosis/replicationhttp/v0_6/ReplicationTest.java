// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.openstreetmap.osmosis.core.pipeline.common.TaskRunner;
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
		configureLoggingConsole();
		configureLoggingLevel(Level.FINEST);
		Logger.getLogger("org.openstreetmap.osmosis.replication.v0_6.ReplicationStateWriter").setLevel(Level.INFO);
		
		// Create the primary replication data source.
		MockReplicationSource source = new MockReplicationSource();

		// Create the sequence server for notifying when new sequence numbers
		// are available and connect it to the primary source.
		ReplicationSequenceServer sequenceServer = new ReplicationSequenceServer(8081);
		source.setChangeSink(sequenceServer);

		// Create a replication data writer and receive data from the primary
		// data source (via the sequence server).
		File workingDir1 = dataUtils.newFolder();
		sequenceServer.setChangeSink(new ReplicationWriter(workingDir1));

		// Create a HTTP replication data server using the data from the
		// replication writer, and receive sequence number updates from the
		// sequence server.
		ReplicationDataServer dataServer = new ReplicationDataServer(8081, workingDir1, 8080);

		// Create a HTTP replication data client receiving data from the data
		// server.
		ReplicationDataClient dataClient = new ReplicationDataClient(new InetSocketAddress(8080));

		// Create a replication data writer to receiving data from the HTTP data
		// source.
		File workingDir2 = dataUtils.newFolder();
		dataClient.setChangeSink(new ReplicationWriter(workingDir2));

		// Send sequence through the primary pipeline to ensure the
		// sequence server is running.
		source.sendSequence();

		// Start the HTTP data server and HTTP data client.
		TaskRunner serverRunner = new TaskRunner(dataServer, "data-server");
		TaskRunner clientRunner = new TaskRunner(dataClient, "data-client");
		serverRunner.start();
		clientRunner.start();

		// Send the test replication intervals.
		for (int i = 0; i < 1000; i++) {
			source.sendSequence();
		}

		Thread.sleep(60000);
		// Shut down the pipelines.
		clientRunner.interrupt();
		serverRunner.interrupt();
		clientRunner.join();
		serverRunner.join();
		source.release();
	}
}
