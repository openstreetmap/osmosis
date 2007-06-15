package com.bretth.osm.conduit.sort;

import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.task.ChangeAction;


/**
 * Packages an element with the associated change.
 * 
 * @author Brett Henderson
 */
public class ChangeElement {
	private OsmElement element;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param element
	 *            The element.
	 * @param action
	 *            The action.
	 */
	public ChangeElement(OsmElement element, ChangeAction action) {
		this.element = element;
		this.action = action;
	}
	
	
	/**
	 * @return The element. 
	 */
	public OsmElement getElement() {
		return element;
	}
	
	
	/**
	 * @return The action. 
	 */
	public ChangeAction getAction() {
		return action;
	}
}
