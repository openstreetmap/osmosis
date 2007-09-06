package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Node;


/**
 * Entity container implementation for nodes.
 * 
 * @author Brett Henderson
 */
public class NodeContainer extends EntityContainer {
	private static final long serialVersionUID = 1L;
	
	
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
	public void process(EntityProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getEntity() {
		return node;
	}
}
