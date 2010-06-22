// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.util.Iterator;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.IntegerLongIndexElement;


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
