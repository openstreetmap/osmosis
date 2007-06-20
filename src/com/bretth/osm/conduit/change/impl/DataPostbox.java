package com.bretth.osm.conduit.change.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Element;


/**
 * This class provides a mechanism for a thread to pass data to another thread.
 * Both threads will block until the other is ready.
 * 
 * @author Brett Henderson
 */
public class DataPostbox {
	private Lock lock;
	private Condition dataWaitCondition;
	private Element data;
	private boolean released;
	private boolean complete;
	private boolean dataPosted;
	private boolean outputOkay;
	
	
	/**
	 * Creates a new instance.
	 */
	public DataPostbox() {
		lock = new ReentrantLock();
		dataWaitCondition = lock.newCondition();
		data = null;
		released = false;
		complete = false;
		dataPosted = false;
		outputOkay = true;
	}
	
	
	/**
	 * This is called by the input thread to validate that no errors have
	 * occurred on the output thread.
	 */
	private void checkForOutputErrors() {
		// Check for writing thread error.
		if (released && (!complete)) {
			throw new ConduitRuntimeException("An output error has occurred, aborting.");
		}
	}
	
	
	/**
	 * This is called by the output thread to validate that no errors have
	 * occurred on the input thread.
	 */
	private void checkForInputErrors() {
		// Check for reading thread error.
		if (!outputOkay) {
			throw new ConduitRuntimeException("An input error has occurred, aborting.");
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
			throw new ConduitRuntimeException("Thread was interrupted.", e);
		}
	}
	
	
	/**
	 * Either thread can call this method when they wish to signal the other
	 * thread that an update has occurred.
	 */
	private void signalUpdate() {
		dataWaitCondition.signal();
	}
	
	
	public void put(Element element) {
		lock.lock();
		
		try {
			checkForOutputErrors();
			
			// Wait until the currently posted data is cleared.
			while (dataPosted) {
				waitForUpdate();
				checkForOutputErrors();
			}
			
			// Post the new data.
			data = element;
			dataPosted = true;
			signalUpdate();
			
		} finally {
			lock.unlock();
		}
	}
	
	
	public void complete() {
		lock.lock();
		
		try {
			checkForOutputErrors();
			complete = true;
			signalUpdate();
			
		} finally {
			lock.unlock();
		}
	}
	
	
	public void release() {
		lock.lock();
		
		try {
			released = true;
			signalUpdate();
			
		} finally {
			lock.unlock();
		}
	}
	
	
	public boolean hasNext() {
		lock.lock();
		
		try {
			checkForInputErrors();
			
			// Wait until data is available.
			while (!(dataPosted || complete)) {
				waitForUpdate();
				checkForInputErrors();
			}
			
			return dataPosted;
			
		} finally {
			lock.unlock();
		}
	}
	
	
	public Element getNext() {
		lock.lock();
		
		try {
			if (hasNext()) {
				dataPosted = false;
				signalUpdate();
				
				return data;
				
			} else {
				throw new ConduitRuntimeException("No data is available, should call hasNext first.");
			}
			
		} finally {
			lock.unlock();
		}
	}
	
	
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
