package com.bretth.osm.osmosis.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.Way;


/**
 * Renders OSM data types as xml.
 * 
 * @author Brett Henderson
 */
public class OsmWriter extends ElementWriter {
	
	private NodeWriter nodeWriter;
	private SegmentWriter segmentWriter;
	private WayWriter wayWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public OsmWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		nodeWriter = new NodeWriter("node", indentLevel + 1);
		segmentWriter = new SegmentWriter("segment", indentLevel + 1);
		wayWriter = new WayWriter("way", indentLevel + 1);
	}
	
	
	/**
	 * Begins an element.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void begin(BufferedWriter writer) {
		beginOpenElement(writer);
		addAttribute(writer, "version", "0.3");
		addAttribute(writer, "generator", "Conduit");
		endOpenElement(writer, false);
	}
	
	
	/**
	 * Ends an element.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void end(BufferedWriter writer) {
		closeElement(writer);
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
		nodeWriter.processNode(writer, node);
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
		segmentWriter.processSegment(writer, segment);
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
		wayWriter.processWay(writer, way);
	}
}
