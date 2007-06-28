package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Node;


/**
 * Element container implementation for nodes.
 * 
 * @author Brett Henderson
 */
public class NodeContainer extends ElementContainer {
	private Node node;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param node
	 *            The node to wrap.
	 */
	public NodeContainer(Node node) {
		this.node = node;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ElementProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getElement() {
		return node;
	}
}
