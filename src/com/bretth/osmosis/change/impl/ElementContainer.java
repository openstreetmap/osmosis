package com.bretth.osmosis.change.impl;

import com.bretth.osmosis.data.Element;
import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;
import com.bretth.osmosis.task.Sink;


/**
 * Implementations of this class allow data elements to be processed without the
 * caller knowing their type.
 * 
 * @author Brett Henderson
 */
public abstract class ElementContainer {
	/**
	 * Calls the appropriate sink process method with the contained element.
	 * 
	 * @param sink
	 *            The sink to invoke.
	 */
	public abstract void process(Sink sink);
	
	
	/**
	 * Calls the appropriate change sink method with the contained element and
	 * specified action.
	 * 
	 * @param changeSink
	 *            The change sink to invoke.
	 * @param action
	 *            The action to apply.
	 */
	public abstract void processChange(ChangeSink changeSink, ChangeAction action);
	
	
	/**
	 * Returns the contained element.
	 * 
	 * @return The element.
	 */
	public abstract Element getElement();
}
