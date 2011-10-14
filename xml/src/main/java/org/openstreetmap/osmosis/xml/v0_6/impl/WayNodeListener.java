// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


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
