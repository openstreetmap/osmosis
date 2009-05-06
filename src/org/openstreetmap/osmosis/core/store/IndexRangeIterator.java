// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterates over specific range within an index source. It will skip records until
 * it reaches the beginning of the range, and stop when it reaches the end of
 * the range.
 * 
 * @param <K>
 *            The index key type.
 * @param <T>
 *            The index record being stored.
 * @author Brett Henderson
 */
public class IndexRangeIterator<K, T extends IndexElement<K>> implements Iterator<T> {
	private Iterator<T> source;
	private K beginKey;
	private K endKey;
	private Comparator<K> ordering;
	private boolean nextRecordAvailable;
	private T nextRecord;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The input index source.
	 * @param beginKey
	 *            The first key for which to return data.
	 * @param endKey
	 *            The last key for which to return data.
	 * @param ordering
	 *            The index key ordering to be used for comparing keys.
	 */
	public IndexRangeIterator(Iterator<T> source, K beginKey, K endKey, Comparator<K> ordering) {
		this.source = source;
		this.beginKey = beginKey;
		this.endKey = endKey;
		this.ordering = ordering;
		
		nextRecordAvailable = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		while (!nextRecordAvailable) {
			K key;
			
			if (!source.hasNext()) {
				break;
			}
			
			// Get the next record and its key.
			nextRecord = source.next();
			key = nextRecord.getKey();
			
			// Skip over records with a key lower than beginKey.
			if (ordering.compare(key, beginKey) >= 0) {
				// No more data is available if we've passed endKey.
				if (ordering.compare(nextRecord.getKey(), endKey) > 0) {
					break;
				}
				
				nextRecordAvailable = true;
			}
		}
		
		return nextRecordAvailable;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		nextRecordAvailable = false;
		
		return nextRecord;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
