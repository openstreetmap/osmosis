package com.bretth.osmosis.core.container.v0_5;

import com.bretth.osmosis.core.domain.v0_5.Entity;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
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
	 * Allows the entire dataset to be iterated across.
	 * 
	 * @return An iterator pointing to the start of the collection.
	 */
	public ReleasableIterator<Entity> iterate();
	
	
	/**
	 * Retrieves a specific node by its identifier.
	 * 
	 * @param id
	 *            The id of the node.
	 * @return The node.
	 */
	public Node getNode(Node id);
	
	
	/**
	 * Retrieves a specific way by its identifier.
	 * 
	 * @param id
	 *            The id of the way.
	 * @return The way.
	 */
	public Way getWay(Way id);
	
	
	/**
	 * Retrieves a specific relation by its identifier.
	 * 
	 * @param id
	 *            The id of the relation.
	 * @return The relation.
	 */
	public Relation getRelation(Relation id);
	
	
	/**
	 * Allows all data within a bounding box to be iterated across.
	 * 
	 * @return An iterator pointing to the start of the result data.
	 */
	public ReleasableIterator<Entity> iterateBoundingBox();
}
