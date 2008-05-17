// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.container.v0_6;

import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableIterator;


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
	public Node getNode(long id);
	
	
	/**
	 * Retrieves a specific way by its identifier.
	 * 
	 * @param id
	 *            The id of the way.
	 * @return The way.
	 */
	public Way getWay(long id);
	
	
	/**
	 * Retrieves a specific relation by its identifier.
	 * 
	 * @param id
	 *            The id of the relation.
	 * @return The relation.
	 */
	public Relation getRelation(long id);
	
	
	/**
	 * Allows the entire dataset to be iterated across.
	 * 
	 * @return An iterator pointing to the start of the collection.
	 */
	public ReleasableIterator<EntityContainer> iterate();
	
	
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
	public ReleasableIterator<EntityContainer> iterateBoundingBox(double left, double right, double top, double bottom, boolean completeWays);
}
