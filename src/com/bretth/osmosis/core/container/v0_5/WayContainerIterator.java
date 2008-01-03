// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_5;

import java.util.Iterator;

import com.bretth.osmosis.core.domain.v0_5.Way;


/**
 * Wraps a set of ways into way containers.
 * 
 * @author Brett Henderson
 */
public class WayContainerIterator implements Iterator<WayContainer> {
	private Iterator<Way> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public WayContainerIterator(Iterator<Way> source) {
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
}
