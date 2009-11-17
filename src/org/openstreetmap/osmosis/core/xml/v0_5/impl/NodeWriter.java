// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_5.impl;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a node as xml.
 *
 * @author Brett Henderson
 */
public class NodeWriter extends ElementWriter {
    /**
     * The Tag-writer for writing out
     * the key=value -tags of a node.#
     *
     */
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
		OsmUser user;
		List<Tag> tags;
		
		user = node.getUser();
		
		beginOpenElement();
		addAttribute("id", Long.toString(node.getId()));
		addAttribute("timestamp", node.getFormattedTimestamp(getTimestampFormat()));
		
		if (!user.equals(OsmUser.NONE)) {
			addAttribute("uid", Integer.toString(user.getId()));
			addAttribute("user", user.getName());
		}
		
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
	public void setWriter(final Writer writer) {
		super.setWriter(writer);
		
		tagWriter.setWriter(writer);
	}
}
