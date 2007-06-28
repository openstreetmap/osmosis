package com.bretth.osmosis.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.container.ChangeContainer;
import com.bretth.osmosis.task.ChangeAction;


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
	 * Begins an element.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void begin(BufferedWriter writer) {
		beginOpenElement(writer);
		addAttribute(writer, "version", "0.3");
		addAttribute(writer, "generator", "Osmosis");
		endOpenElement(writer, false);
	}
	
	
	/**
	 * Ends an element.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void end(BufferedWriter writer) {
		if (activeOsmWriter != null) {
			activeOsmWriter.end(writer);
			activeOsmWriter = null;
		}
		
		lastAction = null;
		closeElement(writer);
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
	
	
	private void updateActiveOsmWriter(BufferedWriter writer, ChangeAction action) {
		if (action != lastAction) {
			if (activeOsmWriter != null) {
				activeOsmWriter.end(writer);
			}
			
			activeOsmWriter = getWriterForAction(action);
			
			activeOsmWriter.begin(writer);
			
			lastAction = action;
		}
	}
	
	
	/**
	 * Writes the change in the container.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param changeContainer
	 *            The container holding the change.
	 */
	public void process(BufferedWriter writer, ChangeContainer changeContainer) {
		updateActiveOsmWriter(writer, changeContainer.getAction());
		activeOsmWriter.process(writer, changeContainer.getEntityContainer());
	}
}
