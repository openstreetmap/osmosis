package com.bretth.osm.conduit.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osm.conduit.data.SegmentReference;


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
	 * @param writer
	 *            The writer to send the xml to.
	 * @param segmentReference
	 *            The segmentReference to be processed.
	 */
	public void processSegmentReference(BufferedWriter writer, SegmentReference segmentReference) {
		beginOpenElement(writer);
		addAttribute(writer, "id", Long.toString(segmentReference.getSegmentId()));
		endOpenElement(writer, true);
	}
}
