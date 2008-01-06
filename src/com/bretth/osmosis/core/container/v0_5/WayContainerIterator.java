// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Wraps a set of ways into way containers.
 * 
 * @author Brett Henderson
 */
public class WayContainerIterator implements ReleasableIterator<WayContainer> {
	private ReleasableIterator<Way> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public WayContainerIterator(ReleasableIterator<Way> source) {
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
	public WayContainer next() {
		return new WayContainer(source.next());
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
