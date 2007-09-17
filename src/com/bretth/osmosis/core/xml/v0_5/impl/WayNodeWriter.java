package com.bretth.osmosis.core.xml.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a way node as xml.
 * 
 * @author Brett Henderson
 */
public class WayNodeWriter extends ElementWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public WayNodeWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
	}
	
	
	/**
	 * Writes the way node.
	 * 
	 * @param wayNode
	 *            The wayNode to be processed.
	 */
	public void processWayNode(WayNode wayNode) {
		beginOpenElement();
		addAttribute("ref", Long.toString(wayNode.getNodeId()));
		endOpenElement(true);
	}
}
