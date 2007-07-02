package com.bretth.osmosis.mysql.impl;

import com.bretth.osmosis.data.Node;


/**
 * A data class representing a node history record.
 * @author Brett Henderson
 *
 */
public class NodeHistory {
	
	private Node node;
	private boolean visible;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param node
	 *            The contained node.
	 * @param visible
	 *            The visible field.
	 */
	public NodeHistory(Node node, boolean visible) {
		this.node = node;
		this.visible = visible;
	}
	
	
	/**
	 * Gets the contained node.
	 * 
	 * @return The node.
	 */
	public Node getNode() {
		return node;
	}
	
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return The visible flag.
	 */
	public boolean isVisible() {
		return visible;
	}
}
