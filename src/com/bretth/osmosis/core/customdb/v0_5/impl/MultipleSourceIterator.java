package com.bretth.osmosis.core.customdb.v0_5.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Allows the contents of several iterators to be combined into a single
 * iterator.
 * 
 * @param <T>
 *            The type of data to be iterated over.
 * @author Brett Henderson
 */
public class MultipleSourceIterator<T> implements Iterator<T> {
	
	private List<Iterator<T>> sources;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sources
	 *            The input iterators.
	 */
	public MultipleSourceIterator(List<Iterator<T>> sources) {
		this.sources = new LinkedList<Iterator<T>>(sources);
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
				sources.remove(0);
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
	
}
