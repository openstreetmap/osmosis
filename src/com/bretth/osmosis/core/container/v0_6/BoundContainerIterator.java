// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_6;

import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Wraps a set of bound items into bound containers.
 * 
 * @author Brett Henderson
 */
public class BoundContainerIterator implements ReleasableIterator<BoundContainer> {
	private ReleasableIterator<Bound> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public BoundContainerIterator(ReleasableIterator<Bound> source) {
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
	public BoundContainer next() {
		return new BoundContainer(source.next());
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
		source.release();
	}
}
