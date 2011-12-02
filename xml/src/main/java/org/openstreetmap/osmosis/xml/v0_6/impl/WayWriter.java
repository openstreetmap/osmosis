// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Renders a way as xml.
 *
 * @author Brett Henderson
 */
public class WayWriter extends EntityWriter {
    /**
     * Write the ordered list of node-references of a way.
     */
    private WayNodeWriter wayNodeWriter;
    /**
     * Write the tags of a way.
     */
    private TagWriter tagWriter;


	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public WayWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
		wayNodeWriter = new WayNodeWriter("nd", indentLevel + 1);
	}
	
	
	/**
	 * Writes the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void process(Way way) {
		List<WayNode> wayNodes;
		Collection<Tag> tags;
		
		beginOpenElement();
		addCommonAttributes(way);
		addMetatags(way);
		
		wayNodes = way.getWayNodes();
		tags = way.getTags();
		
		if (wayNodes.size() > 0 || tags.size() > 0) {
			endOpenElement(false);

			for (WayNode wayNode : wayNodes) {
				wayNodeWriter.processWayNode(wayNode);
			}
			
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
		
		wayNodeWriter.setWriter(writer);
		tagWriter.setWriter(writer);
	}
}
