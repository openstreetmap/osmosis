package com.bretth.osm.conduit.xml.impl;

import java.io.BufferedWriter;
import java.util.List;

import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;


/**
 * Renders a way as xml.
 * 
 * @author Brett Henderson
 */
public class WayWriter extends ElementWriter {
	private SegmentReferenceWriter segmentReferenceWriter;
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
		segmentReferenceWriter = new SegmentReferenceWriter("seg", indentLevel + 1);
	}
	
	
	/**
	 * Writes the way.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param way
	 *            The way to be processed.
	 */
	public void processWay(BufferedWriter writer, Way way) {
		List<SegmentReference> segmentReferences;
		List<Tag> tags;
		
		beginOpenElement(writer);
		addAttribute(writer, "id", Long.toString(way.getId()));
		addAttribute(writer, "timestamp", formatDate(way.getTimestamp()));
		
		segmentReferences = way.getSegmentReferenceList();
		tags = way.getTagList();
		
		if (segmentReferences.size() > 0 || tags.size() > 0) {
			endOpenElement(writer, false);

			for (SegmentReference segmentReference : segmentReferences) {
				segmentReferenceWriter.processSegmentReference(writer, segmentReference);
			}
			
			for (Tag tag : tags) {
				tagWriter.processTag(writer, tag);
			}
			
			closeElement(writer);
			
		} else {
			endOpenElement(writer, true);
		}
	}
}
