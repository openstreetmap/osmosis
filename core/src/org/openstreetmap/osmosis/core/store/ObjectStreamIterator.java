// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * This class reads objects from an ObjectInputStream until the end of stream is
 * reached.
 * 
 * @param <T>
 *            The type of data to be returned by the iterator.
 * @author Brett Henderson
 */
public class ObjectStreamIterator<T> extends ObjectDataInputIterator<T> implements ReleasableIterator<T> {
	private static final Logger LOG = Logger.getLogger(ObjectStreamIterator.class.getName());
	
	private DataInputStream inStream;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inStream
	 *            The stream to read objects from.
	 * @param objectReader
	 *            The reader containing the objects to be deserialized.
	 */
	public ObjectStreamIterator(DataInputStream inStream, ObjectReader objectReader) {
		super(objectReader);
		
		this.inStream = inStream;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				// We cannot throw an exception within a release method.
				LOG.log(Level.WARNING, "Unable to close input stream.", e);
			}
			
			inStream = null;
		}
	}
}
