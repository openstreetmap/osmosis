// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import java.io.BufferedWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a node as xml.
 * 
 * @author Brett Henderson
 */
public class NodeWriter extends ElementWriter {
	private TagWriter tagWriter;
	private NumberFormat numberFormat;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public NodeWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
		
		// Only write the first 7 decimal places.
		// Write in US locale so that a '.' is used as the decimal separator.
		numberFormat = new DecimalFormat(
			"0.#######;-0.#######",
			new DecimalFormatSymbols(Locale.US)
		);
	}
	
	
	/**
	 * Writes the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void process(Node node) {
		List<Tag> tags;
		
		beginOpenElement();
		addAttribute("id", Long.toString(node.getId()));
		addAttribute("timestamp", node.getFormattedTimestamp(getTimestampFormat()));
		if (node.getUserName() != null && node.getUserName().length() > 0) {
			addAttribute("user", node.getUserName());
		}
		if (node.getUserId() != OsmUser.USER_ID_NONE) {
			addAttribute("uid", Integer.toString(node.getUserId()));
		}
		addAttribute("version", Integer.toString(node.getVersion()));
		addAttribute("lat", numberFormat.format(node.getLatitude()));
		addAttribute("lon", numberFormat.format(node.getLongitude()));
		
		tags = node.getTagList();
		
		if (tags.size() > 0) {
			endOpenElement(false);
			
			for (Tag tag : tags) {
				tagWriter.process(tag);
			}
			
			closeElement();
			
		} else {
			endOpenElement(true);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(BufferedWriter writer) {
		super.setWriter(writer);
		
		tagWriter.setWriter(writer);
	}
}
