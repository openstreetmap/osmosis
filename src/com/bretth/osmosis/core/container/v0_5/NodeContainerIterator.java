package com.bretth.osmosis.core.container.v0_5;

import java.util.Iterator;

import com.bretth.osmosis.core.domain.v0_5.Node;


/**
 * Wraps a set of nodes into node containers.
 * 
 * @author Brett Henderson
 */
public class NodeContainerIterator implements Iterator<NodeContainer> {
	private Iterator<Node> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public NodeContainerIterator(Iterator<Node> source) {
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
}
