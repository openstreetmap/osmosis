// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;
import org.openstreetmap.osmosis.replication.common.ServerStateReader;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.ReplicationDataServerChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClient;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceNumberClientChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceNumberClientControl;
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
	private Lock controlLock;
	private Condition controlCondition;
	private boolean clientRunning;


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

		controlLock = new ReentrantLock();
		controlCondition = controlLock.newCondition();
	}


	private long getCurrentSequenceNumber() {
		try {
			return new ServerStateReader().getServerState(dataDirectory.toURI().toURL()).getSequenceNumber();
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("Unable to get the current sequence number", e);
		}
	}


	/**
	 * Either thread can call this method when they wish to wait until an update
	 * has been performed by the other thread.
	 */
	private void waitForUpdate() {
		try {
			controlCondition.await();

		} catch (InterruptedException e) {
			throw new OsmosisRuntimeException("Thread was interrupted.", e);
		}
	}


	/**
	 * Either thread can call this method when they wish to signal the other
	 * thread that an update has occurred.
	 */
	private void signalUpdate() {
		controlCondition.signal();
	}


	@Override
	public void run() {
		controlLock.lock();

		try {
			// Instantiate the replication data server, and the client for
			// receiving
			// sequence number updates.
			SequenceServer server = new SequenceServer(port, new ReplicationDataServerChannelPipelineFactory(
					dataDirectory));
			ClientControl control = new ClientControl(server);
			SequenceNumberClientChannelPipelineFactory channelPipelineFactory =
					new SequenceNumberClientChannelPipelineFactory(control);
			SequenceClient<SequenceNumberClientControl> client = new SequenceClient<SequenceNumberClientControl>(
					new InetSocketAddress(notificationPort), channelPipelineFactory);

			try {
				// Start the server with the current replication number.
				server.start(getCurrentSequenceNumber());

				// Run the client within a loop to allow client restarts if
				// problems occur.
				while (true) {
					clientRunning = true;
					client.start();

					// Wait for the client to stop.
					while (clientRunning) {
						waitForUpdate();
					}

					// Wait for 1 minute between connection failures.
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						throw new OsmosisRuntimeException(
								"Thread sleep failed between sequence number client invocations", e);
					}
				}

			} finally {
				client.stop();
				server.stop();

			}
		} finally {
			controlLock.unlock();
		}
	}

	/**
	 * Internal class used to process event updates from the sequence number
	 * client.
	 * 
	 * @author Brett Henderson
	 */
	private class ClientControl implements SequenceNumberClientControl {

		private SequenceServer server;


		/**
		 * Creates a new instance.
		 * 
		 * @param server
		 *            The server to receive sequence number updates.
		 */
		public ClientControl(SequenceServer server) {
			this.server = server;
		}


		@Override
		public void notifySequenceNumber(long sequenceNumber) {
			// We pass updated sequence numbers directly to the replication data
			// server so that it can send the corresponding data to connected
			// clients..
			server.update(sequenceNumber);
		}


		@Override
		public void channelClosed() {
			controlLock.lock();

			try {
				// The client has failed. We need to tell the master thread so
				// that it can act accordingly (eg. restart the client).
				clientRunning = false;
				signalUpdate();

			} finally {
				controlLock.unlock();
			}
		}
	}
}
