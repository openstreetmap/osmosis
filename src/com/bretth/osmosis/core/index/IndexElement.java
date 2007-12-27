package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.Storeable;


/**
 * Defines the methods to be implemented by data classes stored within an index.
 * 
 * @author Brett Henderson
 */
public interface IndexElement extends Storeable {
	/**
	 * Returns the identifier associated with this element.
	 * 
	 * @return The identifier represented as a long.
	 */
	long getId();
}
