package com.bretth.osm.conduit.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.Sink;


/**
 * An OSM data sink for storing all data to an xml file.
 * 
 * @author Brett Henderson
 */
public class XmlWriter implements Sink {
	
	/**
	 * Defines the characters that must be replaced by an encoded string when writing to XML.
	 */
	private final static Map<Character, String> xmlEncoding;
	
	static {
		// Define all the characters and their encodings.
		xmlEncoding = new HashMap<Character, String>();
		
		xmlEncoding.put(new Character('<'), "&lt;");
		xmlEncoding.put(new Character('>'), "&gt;");
		xmlEncoding.put(new Character('"'), "&quot;");
		xmlEncoding.put(new Character('\''), "&apos;");
		xmlEncoding.put(new Character('&'), "&amp;");
		xmlEncoding.put(new Character('\n'), "&#xA;");
		xmlEncoding.put(new Character('\r'), "&#xD;");
		xmlEncoding.put(new Character('\t'), "&#x9;");
	}
	
	
	private File file;
	private boolean initialized;
	private BufferedWriter writer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to write.
	 */
	public XmlWriter(File file) {
		this.file = file;
	}
	
	
	/**
	 * A utility method for encoding data in XML format.
	 * 
	 * @param data
	 *            The data to be formatted.
	 * 
	 * @return The formatted data. This may be the input string if no changes
	 *         are required.
	 */
	private String escapeData(String data) {
		StringBuffer buffer = null;
		
		for (int i = 0; i < data.length(); ++i) {
			String replacement = xmlEncoding.get(new Character(data.charAt(i)));
			
			if (replacement != null) {
				if (buffer == null)
					buffer = new StringBuffer(data.substring(0, i));
				buffer.append(replacement);
				
			} else if (buffer != null)
				buffer.append(data.charAt(i));
		}
		
		if (buffer == null) {
			return data;
		} else {
			return buffer.toString();
		}
	}
	
	
	/**
	 * A utility method for encoding a data in the correct OSM format.
	 * 
	 * @param date
	 *            The date to be formatted.
	 * 
	 * @return The string representing the date.
	 */
	private String formatDate(Date date) {
		if (date != null) {
			// TODO: Complete data formatting.
			return date.toString();
		} else {
			return "";
		}
	}
	
	
	/**
	 * Writes data to the output file.
	 * 
	 * @param data
	 *            The data to be written.
	 */
	private void write(String data) {
		try {
			writer.write(data);
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes a new line in the output file.
	 */
	private void writeNewLine() {
		try {
			writer.newLine();
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Initialises the output file for writing.
	 */
	private void initialize() {
		if (!initialized) {
			try {
				writer = new BufferedWriter(new FileWriter(file));
				
			} catch (IOException e) {
				throw new ConduitRuntimeException("Unable to open file for writing.", e);
			}
			
			initialized = true;
			
			write("<?xml version='1.0' encoding='UTF-8'?>");
			writeNewLine();
			write("<osm version='0.3' generator='Transformer'>");
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes tags to the output file as xml.
	 * 
	 * @param tags
	 *            The tags to be written.
	 */
	private void writeTags(List<Tag> tags) {
		for (Tag tag : tags) {
			write(
				"        <tag k=\"" + escapeData(tag.getKey())
				+ "\" v=\"" + escapeData(tag.getValue()) + "\" />"
			);
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes segment references to the output file as xml.
	 * 
	 * @param segRefs
	 *            The segment references to be written.
	 */
	private void writeSegmentReferences(List<SegmentReference> segRefs) {
		for (SegmentReference segRef : segRefs) {
			write(
				"        <seg id=\"" + segRef.getSegmentId() + "\" />"
			);
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes a node to the output file.
	 * 
	 * @param node
	 *            The node to be written.
	 */
	public void addNode(Node node) {
		initialize();
		
		write(
			"    <node id=\"" + node.getId()
			+ "\" timestamp=\"" + formatDate(node.getTimestamp())
			+ "\" lat=\"" + node.getLatitude()
			+ "\" lon=\"" + node.getLongitude() + "\""
		);
		
		if (node.getTagList().size() > 0) {
			write(" >");
			writeNewLine();
			
			writeTags(node.getTagList());
			
			write("    </node>");
			writeNewLine();
			
		} else {
			write(" />");
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes a segment to the output file.
	 * 
	 * @param segment
	 *            The segment to be written.
	 */
	public void addSegment(Segment segment) {
		initialize();
		
		write(
			"    <segment id=\"" + segment.getId()
			+ "\" from=\"" + segment.getFrom()
			+ "\" to=\"" + segment.getTo() + "\""
		);
		
		if (segment.getTagList().size() > 0) {
			write(" >");
			writeNewLine();
			
			writeTags(segment.getTagList());
			
			write("    </segment>");
			writeNewLine();
			
		} else {
			write(" />");
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes a way to the output file.
	 * 
	 * @param way
	 *            The way to be written.
	 */
	public void addWay(Way way) {
		initialize();
		
		write(
			"    <way id=\"" + way.getId()
			+ "\" timestamp=\"" + formatDate(way.getTimestamp()) + "\""
		);
		
		if (way.getSegmentReferenceList().size() > 0 || way.getTagList().size() > 0) {
			write(" >");
			writeNewLine();
			
			writeSegmentReferences(way.getSegmentReferenceList());
			writeTags(way.getTagList());
			
			write("    </way>");
			writeNewLine();
			
		} else {
			write(" />");
			writeNewLine();
		}
	}
	
	
	/**
	 * Writes the closing XML tags and closes the output file.
	 */
	public void complete() {
		try {
			write("</osm>");
			writeNewLine();
			
			writer.close();
			
		} catch (Exception e) {
			// Do nothing
		} finally {
			writer = null;
			initialized = false;
		}
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		try {
			if (writer != null) {
				writer.close();
			}
			
		} catch (Exception e) {
			// Do nothing
		} finally {
			writer = null;
			initialized = false;
		}
	}
}
