package com.bretth.osm.osmosis.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.task.ChangeAction;


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
		addAttribute(writer, "generator", "Conduit");
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
	 * Writes the node.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param node
	 *            The node to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processNode(BufferedWriter writer, Node node, ChangeAction action) {
		updateActiveOsmWriter(writer, action);
		activeOsmWriter.processNode(writer, node);
	}
	
	
	/**
	 * Writes the segment.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param segment
	 *            The segment to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processSegment(BufferedWriter writer, Segment segment, ChangeAction action) {
		updateActiveOsmWriter(writer, action);
		activeOsmWriter.processSegment(writer, segment);
	}
	
	
	/**
	 * Writes the way.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param way
	 *            The way to be processed.
	 * @param action
	 *            The particular change action to be performed.
	 */
	public void processWay(BufferedWriter writer, Way way, ChangeAction action) {
		updateActiveOsmWriter(writer, action);
		activeOsmWriter.processWay(writer, way);
	}
}
