package com.bretth.osmosis.xml.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osmosis.data.Node;
import com.bretth.osmosis.data.Tag;


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
	 * @param writer
	 *            The writer to send the xml to.
	 * @param node
	 *            The node to be processed.
	 */
	public void processNode(BufferedWriter writer, Node node) {
		List<Tag> tags;
		
		beginOpenElement(writer);
		addAttribute(writer, "id", Long.toString(node.getId()));
		addAttribute(writer, "timestamp", formatDate(node.getTimestamp()));
		addAttribute(writer, "lat", Double.toString(node.getLatitude()));
		addAttribute(writer, "lon", Double.toString(node.getLongitude()));
		
		tags = node.getTagList();
		
		if (tags.size() > 0) {
			endOpenElement(writer, false);
			
			for (Tag tag : tags) {
				tagWriter.processTag(writer, tag);
			}
			
			closeElement(writer);
			
		} else {
			endOpenElement(writer, true);
		}
	}
}
