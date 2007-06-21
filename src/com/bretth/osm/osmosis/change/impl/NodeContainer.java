package com.bretth.osm.osmosis.change.impl;

import com.bretth.osm.osmosis.data.Element;
import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.task.ChangeAction;
import com.bretth.osm.osmosis.task.ChangeSink;
import com.bretth.osm.osmosis.task.Sink;


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
	public void process(Sink sink) {
		sink.processNode(node);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processChange(ChangeSink changeSink, ChangeAction action) {
		changeSink.processNode(node, action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element getElement() {
		return node;
	}
}
