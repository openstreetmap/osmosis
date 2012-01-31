// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.Initializable;


/**
 * <p>
 * This class provides a mechanism for a thread to pass data to another thread.
 * Both threads will block until the other is ready. It supports a single
 * writing thread, and a single reading thread. Multiple reading or writing
 * threads are NOT supported.
 * </p>
 * <p>
 * The input thread must call methods in the following sequence:
 * <ul>
 * <li>initialize - Called during successful startup, can be skipped in failure
 * condition</li>
 * <li>put - Called from zero to N times until input data is exhausted</li>
 * <li>complete - Only called if input processing completed successfully</li>
 * <li>release - Called once at the end of processing regardless of success or
 * failure</li>
 * </ul>
 * The output thread must call methods in the following sequence:
 * <ul>
 * <li>outputInitialize - Called during successful startup, can be skipped in
 * failure condition</li>
 * <li>hasNext/getNext - Both called until hasNext returns false in which case
 * no more data is available</li>
 * <li>outputComplete - Only called if output processing completed successfully</li>
 * <li>outputRelease - Called once at the end of processing regardless of
 * success or failure</li>
 * </ul>
 * </p>
 * <p>
 * The input thread will block in the following situations:
 * <ul>
 * <li>initialize has been called, but outputInitialize has not yet been called</li>
 * <li>put has been called, and the buffer is full</li>
 * <li>The complete method has been called, and outputComplete has not yet been
 * called</li>
 * <li>The release method has been called, and outputRelease has not yet been
 * called. This wait must occur to support the scenario where both threads
 * subsequently wish to initialize again.</li>
 * </ul>
 * The output thread will block in the following situations:
 * <ul>
 * <li>The outputInitialize method has been called, and initialize has not yet
 * been called</li>
 * <li>hasNext has been called, the buffer is empty, and complete has not yet
 * been called</li>
 * <li>The outputComplete method has been called, but complete has not yet been
 * called (Should never happen because hasNext won't return false until complete
 * has been called).</li>
 * <li>The outputRelease method has been called, but release has not yet been
 * called.</li>
 * </ul>
 * </p>
 * <p>
 * This class may be re-used multiple times. For this to work, both input and
 * output methods must be called an equal number of times or deadlock will
 * occur. Re-use may occur after input or output threads fail, however in all
 * cases calls to release and outputRelease must be matched.
 * </p>
 * 
 * @param <T>
 *            The type of data held in the postbox.
 */
public class DataPostbox<T> implements Initializable {
	private int bufferCapacity;
	private int chunkSize;
	private Lock lock;
	private Condition dataWaitCondition;
	private Map<String, Object> processingMetaData;
	private Collection<T> centralQueue;
	private Collection<T> inboundQueue;
	private Queue<T> outboundQueue;
	private boolean inputInitialized;
	private boolean outputInitialized;
	private boolean inputComplete;
	private boolean outputComplete;
	private boolean inputReleased;
	private boolean outputReleased;
	private boolean inputExit;
	private boolean outputExit;
	private boolean inputOkay;
	private boolean outputOkay;


	/**
	 * Creates a new instance.
	 * 
	 * @param capacity
	 *            The maximum number of objects to hold in the postbox before
	 *            blocking.
	 */
	public DataPostbox(int capacity) {
		if (capacity <= 0) {
			throw new OsmosisRuntimeException("A capacity of " + capacity + " is invalid, must be greater than 0.");
		}

		this.bufferCapacity = capacity;

		// Use a chunk size one quarter of total buffer size. This is a magic
		// number but performance isn't highly sensitive to this parameter.
		chunkSize = bufferCapacity / 4;
		if (chunkSize <= 0) {
			chunkSize = 1;
		}

		// Create the thread synchronisation primitives.
		lock = new ReentrantLock();
		dataWaitCondition = lock.newCondition();

		// Thread synchronisation flags. Each thread moves in lockstep through
		// each of these phases. Only initialize and or complete flags may be
		// skipped which trigger error conditions and the setting of the okay
		// flags.
		inputInitialized = false;
		outputInitialized = false;
		inputComplete = false;
		outputComplete = false;
		inputReleased = false;
		outputReleased = false;
		inputExit = true;
		outputExit = true;
		inputOkay = true;
		outputOkay = true;

		// Create the inter-thread data transfer queues.
		initializeQueues();
	}


	private void initializeQueues() {
		// Create buffer objects.
		centralQueue = new ArrayList<T>();
		inboundQueue = new ArrayList<T>();
		outboundQueue = new ArrayDeque<T>();
	}


	/**
	 * This is called by the input thread to validate that no errors have
	 * occurred on the output thread.
	 */
	private void checkForOutputErrors() {
		// Check for reading thread error.
		if (!outputOkay) {
			throw new OsmosisRuntimeException("An output error has occurred, aborting.");
		}
	}


	/**
	 * This is called by the output thread to validate that no errors have
	 * occurred on the input thread.
	 */
	private void checkForInputErrors() {
		// Check for writing thread error.
		if (!inputOkay) {
			throw new OsmosisRuntimeException("An input error has occurred, aborting.");
		}
	}


	/**
	 * Either thread can call this method when they wish to wait until an update
	 * has been performed by the other thread.
	 */
	private void waitForUpdate() {
		try {
			dataWaitCondition.await();

		} catch (InterruptedException e) {
			throw new OsmosisRuntimeException("Thread was interrupted.", e);
		}
	}


	/**
	 * Either thread can call this method when they wish to signal the other
	 * thread that an update has occurred.
	 */
	private void signalUpdate() {
		dataWaitCondition.signal();
	}


	/**
	 * Adds a group of objects to the central queue ready for consumption by the
	 * receiver.
	 * 
	 * @param o
	 *            The objects to be added.
	 */
	private void populateCentralQueue() {
		lock.lock();

		try {
			checkForOutputErrors();

			// Wait until the currently posted data is cleared.
			while (centralQueue.size() >= bufferCapacity) {
				waitForUpdate();
				checkForOutputErrors();
			}

			// Post the new data.
			centralQueue.addAll(inboundQueue);
			inboundQueue.clear();
			signalUpdate();

		} finally {
			lock.unlock();
		}
	}


	/**
	 * Empties the contents of the central queue into the outbound queue.
	 */
	private void consumeCentralQueue() {
		lock.lock();

		try {
			checkForInputErrors();

			// Wait until data is available.
			while (!((centralQueue.size() > 0) || inputComplete)) {
				waitForUpdate();
				checkForInputErrors();
			}

			outboundQueue.addAll(centralQueue);
			centralQueue.clear();

			signalUpdate();

		} finally {
			lock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Map<String, Object> metaData) {
		if (inputInitialized) {
			throw new OsmosisRuntimeException("initialize has already been called");
		}
		
		lock.lock();

		try {
			checkForOutputErrors();

			// Set the processing metadata, and flag that we have initialized.
			processingMetaData = metaData;
			inputInitialized = true;

			signalUpdate();

			// Now we must wait until the output thread initializes or
			// encounters an error.
			while (!outputInitialized) {
				waitForUpdate();
				checkForOutputErrors();
			}

		} finally {
			lock.unlock();
		}
	}


	/**
	 * Adds a new object to the postbox.
	 * 
	 * @param o
	 *            The object to be added.
	 */
	public void put(T o) {
		if (!inputInitialized) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}

		inboundQueue.add(o);

		if (inboundQueue.size() >= chunkSize) {
			populateCentralQueue();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		if (!inputInitialized) {
			throw new OsmosisRuntimeException("initialize has not been called");
		}

		lock.lock();

		try {
			populateCentralQueue();

			inputComplete = true;

			signalUpdate();

			// Now we must wait until the output thread completes or
			// encounters an error.
			while (!outputComplete) {
				waitForUpdate();
				checkForOutputErrors();
			}

		} finally {
			lock.unlock();
		}
	}


	/**
	 * This method conforms to the
	 * {@link org.openstreetmap.osmosis.core.lifecycle.Releasable} contract,
	 * however there are limitations around calling it multiple times. Each call
	 * to this method must be matched by a call to the outputRelease method in a
	 * separate thread or deadlock will occur.
	 */
	@Override
	public void release() {
		lock.lock();

		try {
			// If release is being called without having completed successfully,
			// it is an error condition.
			if (!inputComplete) {
				inputOkay = false;
			}

			inputReleased = true;
			inputExit = false;
			signalUpdate();

			// Wait until the output thread releases.
			while (!outputReleased) {
				waitForUpdate();
			}

			// At this point both threads have reached a release state so we can
			// reset our state.
			initializeQueues();
			inputInitialized = false;
			inputComplete = false;
			inputReleased = false;
			inputExit = true;
			inputOkay = true;
			signalUpdate();

			// Wait for the output thread to exit.
			while (!outputExit) {
				waitForUpdate();
			}

		} finally {
			lock.unlock();
		}
	}


	/**
	 * Notifies that the output thread has begun processing, and gets the
	 * initialization data set by the input thread. This will block until either
	 * the input thread has called initialize, or an input error occurs.
	 * 
	 * @return The initialization data.
	 */
	public Map<String, Object> outputInitialize() {
		if (outputInitialized) {
			throw new OsmosisRuntimeException("outputInitialize has already been called");
		}
		
		lock.lock();

		try {
			checkForInputErrors();

			// We must wait until the input thread initializes or
			// encounters an error.
			while (!inputInitialized) {
				waitForUpdate();
				checkForInputErrors();
			}

			outputInitialized = true;
			signalUpdate();

			return processingMetaData;

		} finally {
			lock.unlock();
		}
	}


	/**
	 * Indicates if data is available for output. This will block until either
	 * data is available, input processing has completed, or an input error
	 * occurs.
	 * 
	 * @return True if data is available.
	 */
	public boolean hasNext() {
		int queueSize;

		if (!outputInitialized) {
			throw new OsmosisRuntimeException("outputInitialize has not been called");
		}

		queueSize = outboundQueue.size();

		if (queueSize <= 0) {
			consumeCentralQueue();
			queueSize = outboundQueue.size();
		}

		return queueSize > 0;
	}


	/**
	 * Returns the next available object from the postbox. This should be
	 * preceeded by a call to hasNext.
	 * 
	 * @return The next available object.
	 */
	public T getNext() {
		if (hasNext()) {
			T result;

			result = outboundQueue.remove();

			return result;

		} else {
			throw new OsmosisRuntimeException("No data is available, should call hasNext first.");
		}
	}


	/**
	 * Notifies that the output thread has completed processing. This will block
	 * until either the input thread has called complete, or an input error
	 * occurs.
	 */
	public void outputComplete() {
		if (!outputInitialized) {
			throw new OsmosisRuntimeException("outputInitialize has not been called");
		}
		
		lock.lock();

		try {
			checkForInputErrors();

			// We must wait until the input thread completes or encounters an
			// error.
			while (!inputComplete) {
				waitForUpdate();
				checkForInputErrors();
			}

			outputComplete = true;
			signalUpdate();

		} finally {
			lock.unlock();
		}
	}


	/**
	 * Notifies that the output thread has released. This will block until the
	 * input thread has also released and the object has been reset.
	 */
	public void outputRelease() {
		lock.lock();

		try {
			// If release is being called without having completed successfully,
			// it is an error condition.
			if (!outputComplete) {
				outputOkay = false;
				signalUpdate();
			}
			
			// Wait until the input thread is released.
			while (!inputReleased) {
				waitForUpdate();
			}

			// At this point both threads have reached a release state so we can
			// set out state as released but waiting for exit.
			outputInitialized = false;
			outputComplete = false;
			outputReleased = true;
			outputExit = false;
			outputOkay = true;
			signalUpdate();

			// Wait until the input thread has reached the exit point.
			while (!inputExit) {
				waitForUpdate();
			}
			
			// The input thread has reached exit, so now we can clear the
			// release flag (required so that subsequent iterations if they
			// exist must go through the same handshake sequence) and flag that
			// we've reached exit.
			outputReleased = false;
			outputExit = true;
			signalUpdate();

		} finally {
			lock.unlock();
		}
	}
}
