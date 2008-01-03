package com.bretth.osmosis.core.container.v0_5;

import java.util.Iterator;

import com.bretth.osmosis.core.domain.v0_5.Relation;


/**
 * Wraps a set of relations into relation containers.
 * 
 * @author Brett Henderson
 */
public class RelationContainerIterator implements Iterator<RelationContainer> {
	private Iterator<Relation> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public RelationContainerIterator(Iterator<Relation> source) {
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
}
