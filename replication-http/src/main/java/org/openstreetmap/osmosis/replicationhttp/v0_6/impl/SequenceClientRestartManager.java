// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * This class encapsulates the functionality required to manage restarts of a
 * sequence client on failure.
 * 
 * @author Brett Henderson
 */
public class SequenceClientRestartManager {

	private static final Logger LOG = Logger.getLogger(SequenceClientRestartManager.class.getName());
	private static final int RESTART_DELAY = 60000;

	private Lock controlLock;
	private Condition controlCondition;
	private ClientControl control;
	private boolean clientRunning;


	/**
	 * Creates a new instance.
	 */
	public SequenceClientRestartManager() {
		controlLock = new ReentrantLock();
		controlCondition = controlLock.newCondition();

		control = new ClientControl();
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


	/**
	 * Returns a sequence client control object to be used when creating a new
	 * sequence client.
	 * 
	 * @return The sequence client controller.
	 */
	public SequenceClientControl getControl() {
		return control;
	}


	/**
	 * Runs the sequence client and restarts it if it fails. This sequence
	 * client must have been created using a control listener returned from the
	 * getControl method of this object.
	 * 
	 * @param sequenceClient
	 *            The sequence client to manage.
	 */
	public void manageClient(SequenceClient sequenceClient) {
		try {
			// Run the client within a loop to allow client restarts if
			// problems occur.
			while (true) {
				// Initialise the running flag. We must do this before we
				// start the client because the client thread will set it to
				// false when it finishes which may occur very soon after
				// starting.
				clientRunning = true;
				try {
					sequenceClient.start();
				} catch (OsmosisRuntimeException e) {
					// The client startup failed, so log the exception and try
					// again in the next loop.
					LOG.warning("Unable to start the sequence client, will retry in " + RESTART_DELAY
							+ " milliseconds.");
				}

				// Wait for the client to stop.
				try {
					controlLock.lock();
					while (clientRunning) {
						waitForUpdate();
					}
				} finally {
					controlLock.unlock();
				}

				// Stop the client explicitly which will close any remaining
				// resources.
				sequenceClient.stop();

				// Wait for 1 minute between connection failures.
				try {
					Thread.sleep(RESTART_DELAY);
				} catch (InterruptedException e) {
					throw new OsmosisRuntimeException("Thread sleep failed between sequence number client invocations",
							e);
				}
			}

		} finally {
			sequenceClient.stop();
		}
	}

	/**
	 * Internal class used to process event updates from the sequence number
	 * client.
	 * 
	 * @author Brett Henderson
	 */
	private class ClientControl implements SequenceClientControl {

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
