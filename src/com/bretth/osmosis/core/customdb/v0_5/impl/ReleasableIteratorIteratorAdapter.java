// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5.impl;

import java.util.Iterator;

import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Simple class for exposing an Iterator as a ReleasableIterator.
 * 
 * @param <T>
 *            The type of data to be iterated over.
 * @author Brett Henderson
 */
public class ReleasableIteratorIteratorAdapter<T> implements ReleasableIterator<T> {
	
	private Iterator<T> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The source of input data.
	 */
	public ReleasableIteratorIteratorAdapter(Iterator<T> source) {
		this.source = source;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		return source.next();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		source.remove();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
}
