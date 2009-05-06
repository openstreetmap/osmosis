// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_5;

import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Provides access to data within a Dataset. Every thread must access a Dataset
 * through its own reader. A reader must be released after use.
 * 
 * @author Brett Henderson
 */
public interface DatasetReader extends Releasable {
	
	/**
	 * Retrieves a specific node by its identifier.
	 * 
	 * @param id
	 *            The id of the node.
	 * @return The node.
	 */
	Node getNode(long id);
	
	
	/**
	 * Retrieves a specific way by its identifier.
	 * 
	 * @param id
	 *            The id of the way.
	 * @return The way.
	 */
	Way getWay(long id);
	
	
	/**
	 * Retrieves a specific relation by its identifier.
	 * 
	 * @param id
	 *            The id of the relation.
	 * @return The relation.
	 */
	Relation getRelation(long id);
	
	
	/**
	 * Allows the entire dataset to be iterated across.
	 * 
	 * @return An iterator pointing to the start of the collection.
	 */
	ReleasableIterator<EntityContainer> iterate();
	
	
	/**
	 * Allows all data within a bounding box to be iterated across.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 * @param completeWays
	 *            If true, all nodes within the ways will be returned even if
	 *            they lie outside the box.
	 * @return An iterator pointing to the start of the result data.
	 */
	ReleasableIterator<EntityContainer> iterateBoundingBox(
			double left, double right, double top, double bottom, boolean completeWays);
}
