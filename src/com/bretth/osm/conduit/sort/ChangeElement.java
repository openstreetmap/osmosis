package com.bretth.osm.conduit.sort;

import com.bretth.osm.conduit.data.Element;
import com.bretth.osm.conduit.task.ChangeAction;


/**
 * Packages an element with the associated change.
 * 
 * @author Brett Henderson
 */
public class ChangeElement {
	private Element element;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param element
	 *            The element.
	 * @param action
	 *            The action.
	 */
	public ChangeElement(Element element, ChangeAction action) {
		this.element = element;
		this.action = action;
	}
	
	
	/**
	 * @return The element. 
	 */
	public Element getElement() {
		return element;
	}
	
	
	/**
	 * @return The action. 
	 */
	public ChangeAction getAction() {
		return action;
	}
}
