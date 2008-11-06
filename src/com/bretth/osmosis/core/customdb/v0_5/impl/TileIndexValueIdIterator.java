package com.bretth.osmosis.core.customdb.v0_5.impl;

import java.util.Iterator;

import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.store.IntegerLongIndexElement;


/**
 * Iterates over a tile index iterator and returns all the id values stored
 * against the tile index elements.
 * 
 * @author Brett Henderson
 */
public class TileIndexValueIdIterator implements ReleasableIterator<Long> {
	private Iterator<IntegerLongIndexElement> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The input source.
	 */
	public TileIndexValueIdIterator(Iterator<IntegerLongIndexElement> source) {
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
	public Long next() {
		return source.next().getValue();
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
