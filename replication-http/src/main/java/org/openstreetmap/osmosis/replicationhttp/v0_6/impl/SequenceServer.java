// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * This class creates a HTTP server that sends updated replication sequence
 * numbers to clients. Once started it is notified of updated sequence numbers
 * as they occur and will pass these sequence numbers to listening clients.
 * 
 * @author Brett Henderson
 */
public class SequenceServer implements SequenceServerControl {
	
	private static final Logger LOG = Logger.getLogger(SequenceServer.class.getName());
	

	private int port;
	/**
	 * Limits shared data access to one thread at a time.
	 */
	private Lock sharedLock;
	/**
	 * This condition is used to allow the implementation thread and controlling
	 * external thread to signal each other.
	 */
	private Condition controlCondition;
	/**
	 * A flag used only by the external control thread to remember if the server
	 * has been started or not.
	 */
	private boolean masterRunning;
	/**
	 * A flag used to notify the server thread when it is time to stop
	 * processing.
	 */
	private boolean continueRunning;
	/**
	 * A flag set by the server thread when it is running.
	 */
	private boolean threadRunning;
	/**
	 * A flag set by the server thread when it has initialized successfully.
	 */
	private boolean threadInitialized;
	private long sequenceNumber;
	private Thread serverThread;
	private ChannelGroup allChannels;
	private List<Channel> waitingChannels;


	/**
	 * Creates a new instance.
	 * 
	 * @param port
	 *            The port number to listen on.
	 */
	public SequenceServer(int port) {
		this.port = port;

		// Create the thread synchronisation primitives.
		sharedLock = new ReentrantLock();
		controlCondition = sharedLock.newCondition();

		// Create the list of channels waiting to be notified about a new
		// sequence.
		waitingChannels = new ArrayList<Channel>();
	}


	/**
	 * Wait for a signal on controlCondition.
	 */
	private void waitForUpdate() {
		try {
			controlCondition.await();

		} catch (InterruptedException e) {
			throw new OsmosisRuntimeException("Thread was interrupted.", e);
		}
	}


	/**
	 * Send a signal to controlCondition.
	 */
	private void signalUpdate() {
		controlCondition.signal();
	}


	/**
	 * Starts the server.
	 * 
	 * @param initialSequenceNumber
	 *            The initial sequence number.
	 */
	public void start(long initialSequenceNumber) {
		sharedLock.lock();

		try {
			if (masterRunning) {
				throw new OsmosisRuntimeException("The server has already been started");
			}

			// The server thread will wait during startup for this flag to
			// become true.
			continueRunning = false;

			// Create a new thread and launch the server.
			serverThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runServer();
					} catch (Throwable t) {
						LOG.log(Level.SEVERE, "Sequence server thread failed.", t);
					}
				}
			}, "sequence-server-controller");
			serverThread.start();

			// Wait for the thread to start running. It will start but wait for
			// masterRunning to become true before proceeding.
			while (!threadRunning) {
				waitForUpdate();
			}

			// Tell the server thread to proceed with execution.
			continueRunning = true;
			signalUpdate();

			// Wait for the server to initialize.
			while (!threadInitialized) {
				waitForUpdate();

				// If the thread is no longer running it must have failed during
				// initialization.
				if (!threadRunning) {
					throw new OsmosisRuntimeException("The server failed during startup.");
				}
			}
			
			// Server startup has succeeded.
			masterRunning = true;

		} finally {
			sharedLock.unlock();
		}
	}


	/**
	 * Notifies that server of a new sequence number.
	 * 
	 * @param newSequenceNumber
	 *            The new sequence number.
	 */
	public void update(long newSequenceNumber) {
		if (!masterRunning) {
			throw new OsmosisRuntimeException("The server has not been started");
		}

		// Update the sequence and notify the server thread.
		sharedLock.lock();
		try {
			if (!threadRunning) {
				throw new OsmosisRuntimeException("The server thread is no longer running.");
			}

			this.sequenceNumber = newSequenceNumber;
			signalUpdate();
		} finally {
			sharedLock.unlock();
		}
	}


	/**
	 * Stops the server.
	 */
	public void stop() {
		if (masterRunning) {
			sharedLock.lock();

			// Notify the server thread that it is time to stop processing.
			try {
				// Flag the server thread to stop.
				continueRunning = false;

				signalUpdate();
			} finally {
				sharedLock.unlock();
			}

			// Wait for the server thread to stop.
			try {
				serverThread.join();
			} catch (InterruptedException e) {
				throw new OsmosisRuntimeException("Interrupt occurred while waiting for server thread to exit", e);
			}

			// Clear our control flag.
			masterRunning = false;
		}
	}


	/**
	 * The main server processing method. This must be launched within a
	 * separate thread and will run until told to shutdown.
	 */
	private void runServer() {
		sharedLock.lock();

		// This flag must be set for the duration of the thread execution.
		threadRunning = true;
		signalUpdate();
		try {
			// Wait for the control thread to tell us to proceed.
			while (!continueRunning) {
				waitForUpdate();
			}

			// Create a channel group to hold all channels for use during
			// shutdown.
			allChannels = new DefaultChannelGroup("sequence-server");

			// Create the processing thread pools.
			ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());

			try {
				// Launch the server.
				ServerBootstrap bootstrap = new ServerBootstrap(factory);
				bootstrap.setPipelineFactory(new SequenceServerChannelPipelineFactory(this));
				bootstrap.setOption("child.tcpNoDelay", true);
				bootstrap.setOption("child.keepAlive", true);
				allChannels.add(bootstrap.bind(new InetSocketAddress(port)));
				
				// Notify the external control thread that we've initialized
				// successfully.
				threadInitialized = true;
				signalUpdate();

				// Process until we're told to shutdown.
				do {
					/*
					 * Create a new waiting channels list and process from the
					 * original. This is necessary because some channels may get
					 * added back in during processing causing a concurrent
					 * modification exception. Due to the Netty implementation,
					 * if a write operation completes before we get a chance to
					 * register the completion listener, the listener will run
					 * within this thread and that will mean the channel will
					 * need to be added to the waiting list before we complete
					 * sending messages to all the other channels.
					 */
					List<Channel> existingWaitingChannels = waitingChannels;
					waitingChannels = new ArrayList<Channel>();
					for (Channel channel : existingWaitingChannels) {
						sendSequenceNumber(channel, sequenceNumber, true);
					}

					waitForUpdate();
				} while (continueRunning);

			} finally {

				// Shutdown the server.
				allChannels.close().awaitUninterruptibly();
				factory.releaseExternalResources();
			}

		} finally {
			threadRunning = false;
			threadInitialized = false;
			signalUpdate();
			
			sharedLock.unlock();
		}
	}


	/**
	 * Sends the specified sequence number to the channel. If follow is
	 * specified, the channel will be held open and follow up calls will be made
	 * to determineNextChannelAction with this channel and sequence number when
	 * the operation completes. If follow is not specified, the channel will be
	 * closed when the operation completes.
	 * 
	 * @param channel
	 *            The channel.
	 * @param currentSequenceNumber
	 *            The sequence number to be sent.
	 * @param follow
	 *            If true, the channel will be held open and updated sequences
	 *            sent as they are arrive.
	 */
	private void sendSequenceNumber(final Channel channel, final long currentSequenceNumber, boolean follow) {
		// Write the sequence number to the channel.
		ChannelFuture future = channel.write(currentSequenceNumber);

		if (follow) {
			// Upon completion of this write, check to see whether a new
			// sequence must be sent or whether we should wait for further
			// updates.
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// Only send more data if the write was successful.
					if (future.isSuccess()) {
						determineNextChannelAction(channel, currentSequenceNumber);
					}
				}
			});
		} else {
			// Upon completion of this write, close the channel.
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					channel.close();
				}
			});
		}
	}


	/**
	 * Checks to see if the current sequence number should be send to the
	 * channel, or whether the channel must wait for a new sequence number to
	 * arrive.
	 * 
	 * @param channel
	 *            The channel.
	 * @param lastSequenceNumber
	 *            The last sequence number sent to the channel.
	 */
	private void determineNextChannelAction(Channel channel, long lastSequenceNumber) {
		long currentSequenceNumber;
		boolean upToDate;

		// We can only access the master sequence number and waiting channels
		// while we have the lock
		sharedLock.lock();
		try {
			currentSequenceNumber = sequenceNumber;

			// Check if the channel has already been sent the current sequence
			// number.
			upToDate = (lastSequenceNumber == currentSequenceNumber);

			// If the channel is up to date we add it to the list waiting for a
			// new sequence notification.
			if (upToDate) {
				waitingChannels.add(channel);
			}
		} finally {
			sharedLock.unlock();
		}

		// If the channel is not up to date, we must send the current sequence.
		if (!upToDate) {
			sendSequenceNumber(channel, currentSequenceNumber, true);
		}
	}


	@Override
	public void sendSequenceNumber(Channel channel, boolean follow) {
		long currentSequenceNumber;

		// Get the current sequence number within the lock.
		sharedLock.lock();
		try {
			currentSequenceNumber = sequenceNumber;
		} finally {
			sharedLock.unlock();
		}

		sendSequenceNumber(channel, currentSequenceNumber, follow);
	}


	@Override
	public void registerChannel(Channel channel) {
		allChannels.add(channel);
	}
}
