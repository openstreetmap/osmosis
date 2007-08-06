package com.bretth.osmosis.mysql.impl;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.change.impl.DataPostbox;


/**
 * The main purpose of this class is to avoid timeouts during setup of multiple
 * slow running queries, MySQL will timeout within a relatively short period if
 * no data is read from a query. This class avoids that problem by slowly
 * reading single entities into a buffer until the consumer begins to read data.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity to retrieved.
 */
public class SlowStartEntityReader<T> {
	
	private static AtomicInteger threadIndex = new AtomicInteger(0);
	
	private EntityReader<T> reader;
	private DataPostbox<T> buffer;
	private boolean initialized;
	private T nextValue;
	private boolean nextValueIsPopulated;
	private DataReadingThread<T> dataReadingThread;
	private int slowReadInterval;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param reader
	 *            The reader providing the data. This reader will be released
	 *            after use.
	 * @param bufferCapacity
	 *            The capacity of the buffer.
	 * @param slowReadInterval
	 *            The interval in milliseconds between reading individual
	 *            entities from the underlying reader during slow start.
	 */
	public SlowStartEntityReader(EntityReader<T> reader, int bufferCapacity, int slowReadInterval) {
		this.reader = reader;
		this.slowReadInterval = slowReadInterval;
		
		buffer = new DataPostbox<T>(bufferCapacity);
		initialized = false;
		nextValue = null;
		nextValueIsPopulated = false;
	}
	
	
	/**
	 * Initialises reading from the underlying stream in a separate thread. This
	 * may be called multiple times which will only result in a single
	 * initialisation.
	 */
	private void initialize() {
		if (!initialized) {
			dataReadingThread = new DataReadingThread<T>(reader, buffer, slowReadInterval);
			dataReadingThread.setName(Thread.currentThread().getName() + "-dataReader-" + threadIndex.incrementAndGet());
			
			dataReadingThread.start();
			
			initialized = true;
		}
	}
	
	
	/**
	 * Indicates if there is any more data available to be read.
	 * 
	 * @return True if more data is available, false otherwise.
	 */
	public boolean hasNext() {
		initialize();
		
		if (nextValueIsPopulated) {
			return true;
			
		} else if (buffer.hasNext()) {
			nextValue = buffer.getNext();
			nextValueIsPopulated = true;
			
			return true;
			
		} else {
			return false;
		}
	}
	
	
	/**
	 * Returns the next available entity without advancing to the next record.
	 * 
	 * @return The next available entity.
	 */
	public T peekNext() {
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No value is available.");
		}
		
		return nextValue;
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public T next() {
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No value is available.");
		}
		
		nextValueIsPopulated = false;
		
		// Tells the data reading thread that slow start has finished.
		dataReadingThread.enableFullSpeed();
		
		return nextValue;		
	}
	
	
	/**
	 * Releases all resources owned by this object. This method is guaranteed
	 * not to throw transactions and should always be called in a finally block
	 * whenever this class is used.
	 */
	public void release() {
		// If we haven't initialised yet, the reader must be released because it
		// isn't owned by the data reading thread yet.
		// Otherwise we must notify the data reading thread that we have been
		// released so that it can abort processing if necessary.
		if (!initialized) {
			reader.release();
		} else {
			dataReadingThread.notifyDestinationReleased();
		}
	}
	
	
	/**
	 * Performs the reading of the underlying reader into the buffer in a
	 * separate thread.
	 * 
	 * @author Brett Henderson
	 * @param <T>
	 *            The type of entity to retrieved.
	 */
	private static class DataReadingThread<T> extends Thread {
		private EntityReader<T> reader;
		private DataPostbox<T> buffer;
		private int slowReadInterval;
		private boolean fullSpeed;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param reader
		 *            The reader providing the data.
		 * @param buffer
		 *            The buffer receiving data.
		 * @param slowReadInterval
		 *            The interval in milliseconds between reading individual
		 *            entities from the underlying reader during slow start.
		 */
		public DataReadingThread(EntityReader<T> reader, DataPostbox<T> buffer, int slowReadInterval) {
			this.reader = reader;
			this.buffer = buffer;
			this.slowReadInterval = slowReadInterval;
			
			fullSpeed = false;
		}
		
		
		/**
		 * Notifies this thread that slow reading is no longer required and full
		 * speed reading may commence.
		 */
		public void enableFullSpeed() {
			fullSpeed = true;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				long lastReadTime;
				
				lastReadTime = new Date().getTime();
				
				while (reader.hasNext()) {
					buffer.put(reader.next());
					
					// Perform slow read logic if full speed hasn't been
					// started.
					if (!fullSpeed) {
						// This loop will explicitly break out when the required time is
						// reached.
						while (true) {
							long currentTime;
							long remainingInterval;
							
							currentTime = new Date().getTime();
							
							// Calculate how many milliseconds until the next read is allowed.
							remainingInterval = lastReadTime + slowReadInterval - currentTime;
							
							// If no time is left or if somehow the clock has
							// gone backwards break out immediately and perform
							// a read.
							// Otherwise, wait for the required amount of time
							// and try again.
							if (remainingInterval <= 0 || remainingInterval > slowReadInterval) {
								long newLastReadTime;
								
								// The last read timestamp must be set to the
								// previous timestamp plus the interval.
								newLastReadTime = lastReadTime + slowReadInterval;
								
								// Modify the time to the current time if it is
								// before now, or if it is more than one
								// interval into the future.
								if (newLastReadTime < currentTime || newLastReadTime > (currentTime + slowReadInterval)) {
									newLastReadTime = currentTime;
								}
								
								lastReadTime = newLastReadTime;
								
								break;
								
							} else {
								try {
									Thread.sleep(remainingInterval);
								} catch (InterruptedException e) {
									// Ignore interruption.
								}
							}
						}
					}
				}
				
				buffer.complete();
				
			} finally {
				reader.release();
				buffer.release();
			}
		}
		
		
		/**
		 * Notifies the thread that the output destination has been released.
		 * This will cause the thread to abort if it is still running.
		 */
		public void notifyDestinationReleased() {
			// If this thread hasn't terminated an error has occurred
			// externally. By setting the output error condition on the buffer,
			// the thread owned by this object will abort.
			if (this.getState() != State.TERMINATED) {
				buffer.setOutputError();
			}
		}
	}
}
