package com.bretth.osmosis.core.xml.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.WayNode;


/**
 * Provides the definition of a class receiving way nodes.
 * 
 * @author Brett Henderson
 */
public interface WayNodeListener {
	/**
	 * Processes the way node.
	 * 
	 * @param wayNode
	 *            The way node.
	 */
	void processWayNode(WayNode wayNode);
}
