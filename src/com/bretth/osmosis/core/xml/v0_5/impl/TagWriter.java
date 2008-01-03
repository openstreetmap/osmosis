// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5.impl;

import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.xml.common.ElementWriter;


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
