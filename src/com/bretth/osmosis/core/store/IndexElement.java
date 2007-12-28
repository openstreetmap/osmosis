package com.bretth.osmosis.core.store;



/**
 * Defines the methods to be implemented by data classes stored within an index.
 * 
 * @author Brett Henderson
 */
public interface IndexElement extends Storeable {
	/**
	 * Returns the identifier associated with this element for the purposes of
	 * indexing. Note that this is always in the form of a long, the element
	 * implementation may choose to use a shorter id internally and in its
	 * persistence mechanism.
	 * 
	 * @return The identifier represented as a long.
	 */
	long getIndexId();
}
