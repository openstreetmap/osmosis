// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.store;

import java.io.DataInputStream;

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
			} catch (Exception e) {
				// Do nothing.
			}
			
			inStream = null;
		}
	}
}
