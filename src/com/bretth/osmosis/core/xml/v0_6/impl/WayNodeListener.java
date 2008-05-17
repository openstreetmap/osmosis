// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.WayNode;


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
