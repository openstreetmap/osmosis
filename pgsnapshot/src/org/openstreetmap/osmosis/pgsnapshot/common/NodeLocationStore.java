// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * A node location store is used for caching node locations that are
 * subsequently used to build way geometries.
 * 
 * @author Brett Henderson
 */
public interface NodeLocationStore extends Releasable {
	/**
	 * Adds the specified node location details.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 * @param nodeLocation
	 *            The geo-spatial location details.
	 */
	void addLocation(long nodeId, NodeLocation nodeLocation);
	
	
	/**
	 * Gets the location details of the specified node.
	 * 
	 * @param nodeId
	 *            The node identifier.
	 * @return The geo-spatial location details. If the node doesn't exist, the
	 *         valid flag will be set to false.
	 */
	NodeLocation getNodeLocation(long nodeId);
}
