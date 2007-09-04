package com.bretth.osmosis.core.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osmosis.core.container.v0_4.ChangeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Renders OSM changes as xml.
 * 
 * @author Brett Henderson
 */
public class OsmChangeWriter extends ElementWriter {
	
	private OsmWriter osmCreateWriter;
	private OsmWriter osmModifyWriter;
	private OsmWriter osmDeleteWriter;
	private OsmWriter activeOsmWriter;
	private ChangeAction lastAction;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public OsmChangeWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		osmCreateWriter = new OsmWriter("create", indentLevel + 1);
		osmModifyWriter = new OsmWriter("modify", indentLevel + 1);
		osmDeleteWriter = new OsmWriter("delete", indentLevel + 1);
		activeOsmWriter = null;
		lastAction = null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(BufferedWriter writer) {
		super.setWriter(writer);
		
		osmCreateWriter.setWriter(writer);
		osmModifyWriter.setWriter(writer);
		osmDeleteWriter.setWriter(writer);
	}
	
	
	/**
	 * Begins an element.
	 */
	public void begin() {
		beginOpenElement();
		addAttribute("version", XmlConstants.OSM_VERSION);
		addAttribute("generator", "Osmosis");
		endOpenElement(false);
	}
	
	
	/**
	 * Ends an element.
	 */
	public void end() {
		if (activeOsmWriter != null) {
			activeOsmWriter.end();
			activeOsmWriter = null;
		}
		
		lastAction = null;
		closeElement();
	}
	
	
	/**
	 * Returns the appropriate osm writer for the particular change type.
	 * 
	 * @param action
	 *            The change action to be performed.
	 * @return The osm writer for the change type.
	 */
	private OsmWriter getWriterForAction(ChangeAction action) {
		if (action.equals(ChangeAction.Create)) {
			return osmCreateWriter;
		} else if (action.equals(ChangeAction.Modify)) {
			return osmModifyWriter;
		} else if (action.equals(ChangeAction.Delete)) {
			return osmDeleteWriter;
		} else {
			throw new OsmosisRuntimeException("The change action " + action + " is not recognised.");
		}
	}
	
	
	private void updateActiveOsmWriter(ChangeAction action) {
		if (action != lastAction) {
			if (activeOsmWriter != null) {
				activeOsmWriter.end();
			}
			
			activeOsmWriter = getWriterForAction(action);
			
			activeOsmWriter.begin();
			
			lastAction = action;
		}
	}
	
	
	/**
	 * Writes the change in the container.
	 * 
	 * @param changeContainer
	 *            The container holding the change.
	 */
	public void process(ChangeContainer changeContainer) {
		updateActiveOsmWriter(changeContainer.getAction());
		activeOsmWriter.process(changeContainer.getEntityContainer());
	}
}
