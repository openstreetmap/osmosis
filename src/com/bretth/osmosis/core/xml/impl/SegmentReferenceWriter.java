package com.bretth.osmosis.core.xml.impl;

import com.bretth.osmosis.core.domain.SegmentReference;


/**
 * Renders a segment reference as xml.
 * 
 * @author Brett Henderson
 */
public class SegmentReferenceWriter extends ElementWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public SegmentReferenceWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
	}
	
	
	/**
	 * Writes the tag.
	 * 
	 * @param segmentReference
	 *            The segmentReference to be processed.
	 */
	public void processSegmentReference(SegmentReference segmentReference) {
		beginOpenElement();
		addAttribute("id", Long.toString(segmentReference.getSegmentId()));
		endOpenElement(true);
	}
}
