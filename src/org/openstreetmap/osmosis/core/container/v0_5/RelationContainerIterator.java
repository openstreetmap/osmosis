// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_5;

import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


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
	
	public boolean hasNext() {
		return source.hasNext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public RelationContainer next() {
		return new RelationContainer(source.next());
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public void remove() {
		source.remove();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void release() {
		source.release();
	}
}
