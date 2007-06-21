package com.bretth.osmosis.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osmosis.data.Tag;


/**
 * Renders a tag as xml.
 * 
 * @author Brett Henderson
 */
public class TagWriter extends ElementWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public TagWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
	}
	
	
	/**
	 * Writes the tag.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param tag
	 *            The tag to be processed.
	 */
	public void processTag(BufferedWriter writer, Tag tag) {
		beginOpenElement(writer);
		addAttribute(writer, "k", tag.getKey());
		addAttribute(writer, "v", tag.getValue());
		endOpenElement(writer, true);
	}
}
