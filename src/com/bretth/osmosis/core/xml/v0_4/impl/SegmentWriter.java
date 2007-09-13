package com.bretth.osmosis.core.xml.v0_4.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.xml.common.ElementWriter;


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
	 * @param segment
	 *            The segment to be processed.
	 */
	public void process(Segment segment) {
		List<Tag> tags;
		
		beginOpenElement();
		addAttribute("id", Long.toString(segment.getId()));
		addAttribute("from", Long.toString(segment.getFrom()));
		addAttribute("to", Long.toString(segment.getTo()));
		addAttribute("timestamp", formatDate(segment.getTimestamp()));
		if (segment.getUser() != null && segment.getUser().length() > 0) {
			addAttribute("user", segment.getUser());
		}
		
		tags = segment.getTagList();
		
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
