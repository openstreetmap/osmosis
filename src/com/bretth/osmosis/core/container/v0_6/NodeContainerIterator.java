// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_6;

import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Wraps a set of nodes into node containers.
 * 
 * @author Brett Henderson
 */
public class NodeContainerIterator implements ReleasableIterator<NodeContainer> {
	private ReleasableIterator<Node> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public NodeContainerIterator(ReleasableIterator<Node> source) {
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
	public NodeContainer next() {
		return new NodeContainer(source.next());
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
