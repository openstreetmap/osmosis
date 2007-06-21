package com.bretth.osmosis.change.impl;

import com.bretth.osmosis.task.ChangeAction;
import com.bretth.osmosis.task.ChangeSink;


/**
 * Holds an ElementContainer and an associated action.
 * 
 * @author Brett Henderson
 */
public class ChangeContainer {
	private ElementContainer element;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param element
	 *            The element to store.
	 * @param action
	 *            The action to store.
	 */
	public ChangeContainer(ElementContainer element, ChangeAction action) {
		this.element = element;
		this.action = action;
	}
	
	
	/**
	 * Calls the appropriate change sink method with the contained element and
	 * action.
	 * 
	 * @param changeSink
	 *            The change sink to invoke.
	 */
	public void processChange(ChangeSink changeSink) {
		element.processChange(changeSink, action);
	}
	
	
	/**
	 * Returns the contained element.
	 * 
	 * @return The element.
	 */
	public ElementContainer getElement() {
		return element;
	}
	
	
	/**
	 * Returns the contained action.
	 * 
	 * @return The action.
	 */
	public ChangeAction getAction() {
		return action;
	}
}
