// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;



/**
 * Allows the contents of several iterators to be combined into a single
 * iterator.
 * 
 * @param <T>
 *            The type of data to be iterated over.
 * @author Brett Henderson
 */
public class MultipleSourceIterator<T> implements ReleasableIterator<T> {
	
	private List<ReleasableIterator<T>> sources;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sources
	 *            The input iterators.
	 */
	public MultipleSourceIterator(List<ReleasableIterator<T>> sources) {
		this.sources = new LinkedList<ReleasableIterator<T>>(sources);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		while (sources.size() > 0) {
			if (sources.get(0).hasNext()) {
				return true;
			} else {
				sources.remove(0).release();
			}
		}
		
		return false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		return sources.get(0).next();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (ReleasableIterator<T> source : sources) {
			source.release();
		}
	}
	
}
