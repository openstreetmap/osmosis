package com.bretth.osmosis.core.xml.impl;

import com.bretth.osmosis.core.domain.Tag;


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
	 * @param tag
	 *            The tag to be processed.
	 */
	public void process(Tag tag) {
		beginOpenElement();
		addAttribute("k", tag.getKey());
		addAttribute("v", tag.getValue());
		endOpenElement(true);
	}
}
