package com.bretth.osmosis.core.xml.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osmosis.core.data.Node;
import com.bretth.osmosis.core.data.Tag;


/**
 * Renders a node as xml.
 * 
 * @author Brett Henderson
 */
public class NodeWriter extends ElementWriter {
	private TagWriter tagWriter;
	
	
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
		addAttribute("timestamp", formatDate(node.getTimestamp()));
		addAttribute("lat", Double.toString(node.getLatitude()));
		addAttribute("lon", Double.toString(node.getLongitude()));
		
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
