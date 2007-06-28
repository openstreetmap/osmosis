package com.bretth.osmosis.container;

import com.bretth.osmosis.data.Element;


/**
 * Implementations of this class allow data elements to be processed without the
 * caller knowing their type.
 * 
 * @author Brett Henderson
 */
public abstract class ElementContainer {
	/**
	 * Calls the appropriate process method with the contained element.
	 * 
	 * @param processor
	 *            The processor to invoke.
	 */
	public abstract void process(ElementProcessor processor);
	
	
	/**
	 * Returns the contained element.
	 * 
	 * @return The element.
	 */
	public abstract Element getElement();
}
