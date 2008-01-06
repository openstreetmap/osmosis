// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Wraps a set of relations into relation containers.
 * 
 * @author Brett Henderson
 */
public class RelationContainerIterator implements ReleasableIterator<RelationContainer> {
	private ReleasableIterator<Relation> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public RelationContainerIterator(ReleasableIterator<Relation> source) {
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
	public RelationContainer next() {
		return new RelationContainer(source.next());
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
