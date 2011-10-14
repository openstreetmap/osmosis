// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * This class provides a mechanism for a thread to pass data to another thread.
 * Both threads will block until the other is ready. It supports a single
 * writing thread, and a single reading thread. Multiple reading or writing
 * threads are NOT supported.
 * 
 * @param <T>
 *            The type of data held in the postbox.
 */
public class DataPostbox<T> {
	private int bufferCapacity;
	private int chunkSize;
	private Lock lock;
	private Condition dataWaitCondition;
	private Collection<T> centralQueue;
	private Collection<T> inboundQueue;
	private Queue<T> outboundQueue;
	private boolean released;
	private boolean complete;
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
			throw new OsmosisRuntimeException(
				"A capacity of " + capacity
				+ " is invalid, must be greater than 0."
			);
		}
		
		this.bufferCapacity = capacity;
		
		// Use a chunk size one quarter of total buffer size. This is a magic
		// number but performance isn't highly sensitive to this parameter.
		chunkSize = bufferCapacity / 4;
		if (chunkSize <= 0) {
			chunkSize = 1;
		}
		lock = new ReentrantLock();
		dataWaitCondition = lock.newCondition();
		centralQueue = new ArrayList<T>();
		inboundQueue = new ArrayList<T>();
		outboundQueue = new ArrayDeque<T>();
		released = false;
		complete = false;
		outputOkay = true;
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
		if (released && (!complete)) {
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
			while (!((centralQueue.size() > 0) || complete)) {
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
	 * Adds a new object to the postbox.
	 * 
	 * @param o
	 *            The object to be added.
	 */
	public void put(T o) {
		inboundQueue.add(o);
		
		if (inboundQueue.size() >= chunkSize) {
			populateCentralQueue();
		}
	}
	
	
	/**
	 * Marks input is complete.
	 */
	public void complete() {
		lock.lock();
		
		try {
			populateCentralQueue();
			
			complete = true;
			signalUpdate();
			
		} finally {
			lock.unlock();
		}
	}
	
	
	/**
	 * Must be called at the end of input processing regardless of whether
	 * errors have occurred.
	 */
	public void release() {
		lock.lock();
		
		try {
			released = true;
			signalUpdate();
			
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
	 * Allows an output thread to signal that it has failed, this will cause
	 * exceptions to be thrown if more data is sent by input input threads.
	 */
	public void setOutputError() {
		lock.lock();
		
		try {
			outputOkay = false;
			signalUpdate();
			
		} finally {
			lock.unlock();
		}
	}
}
