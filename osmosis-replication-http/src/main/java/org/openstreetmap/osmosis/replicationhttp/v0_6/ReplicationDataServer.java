// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.replication.common.ServerStateReader;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.ReplicationDataServerChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClient;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClientRestartManager;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceNumberClientChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceNumberClientListener;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceServer;


/**
 * This task creates a HTTP server that sends updated replication data to
 * clients. It is notified of updated sequence numbers as they occur by
 * connecting to a replication sequence server.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataServer implements RunnableTask {

	private int notificationPort;
	private File dataDirectory;
	private int port;


	/**
	 * Creates a new instance.
	 * 
	 * @param notificationPort
	 *            The port to connect to for notification updates.
	 * @param dataDirectory
	 *            The location of the replication data and state files.
	 * @param port
	 *            The port to listen on.
	 */
	public ReplicationDataServer(int notificationPort, File dataDirectory, int port) {
		this.notificationPort = notificationPort;
		this.dataDirectory = dataDirectory;
		this.port = port;
	}


	/**
	 * Returns the port that is being used to listen for new connections.
	 * 
	 * @return The port number.
	 */
	public int getPort() {
		return port;
	}


	private long getCurrentSequenceNumber() {
		try {
			return new ServerStateReader().getServerState(dataDirectory.toURI().toURL()).getSequenceNumber();
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("Unable to get the current sequence number", e);
		}
	}


	@Override
	public void run() {
		// Instantiate the replication data server.
		final SequenceServer server = new SequenceServer(port, new ReplicationDataServerChannelPipelineFactory(
				dataDirectory));

		// Configure a listener to send sequence number events from the
		// client to the server.
		SequenceNumberClientListener numberListener = new SequenceNumberClientListener() {
			@Override
			public void notifySequenceNumber(long sequenceNumber) {
				server.update(sequenceNumber);
			}
		};

		// Create a sequence client restart manager so that our sequence
		// client continues processing in the face of temporary connectivity
		// issues.
		SequenceClientRestartManager clientRestartManager = new SequenceClientRestartManager();

		// Create the client for receiving updated sequence numbers..
		SequenceNumberClientChannelPipelineFactory channelPipelineFactory =
				new SequenceNumberClientChannelPipelineFactory(
						clientRestartManager.getControl(), numberListener, "localhost");
		SequenceClient client = new SequenceClient(new InetSocketAddress(notificationPort), channelPipelineFactory);

		try {
			// Start the server with the current replication number.
			server.start(getCurrentSequenceNumber());

			// Update the port. It may have been allocated dynamically if the
			// port was specified as 0.
			port = server.getPort();

			// Run the client and perform restarts if it fails. This call will
			// block.
			clientRestartManager.manageClient(client);

		} finally {
			server.stop();
		}
	}
}
