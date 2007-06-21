package com.bretth.osm.osmosis.xml.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.Tag;


/**
 * Renders a segment as xml.
 * 
 * @author Brett Henderson
 */
public class SegmentWriter extends ElementWriter {
	private TagWriter tagWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public SegmentWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
	}
	
	
	/**
	 * Writes the segment.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param segment
	 *            The segment to be processed.
	 */
	public void processSegment(BufferedWriter writer, Segment segment) {
		List<Tag> tags;
		
		beginOpenElement(writer);
		addAttribute(writer, "id", Long.toString(segment.getId()));
		addAttribute(writer, "from", Long.toString(segment.getFrom()));
		addAttribute(writer, "to", Long.toString(segment.getTo()));
		
		tags = segment.getTagList();
		
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
