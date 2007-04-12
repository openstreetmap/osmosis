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
import com.bretth.osm.conduit.task.OsmSink;


public class XmlWriter implements OsmSink {
	
	private final static Map<Character, String> xmlEncoding;
	
	static {
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
	
	
	public XmlWriter() {
	}
	
	
	public XmlWriter(File file) {
		this.file = file;
	}
	
	
	public void setFile(File file) {
		this.file = file;
	}
	
	
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
	
	
	private String formatDate(Date date) {
		if (date != null) {
			// TODO: Complete data formatting.
			return date.toString();
		} else {
			return "";
		}
	}
	
	
	private void write(String data) {
		try {
			writer.write(data);
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	private void writeNewLine() {
		try {
			writer.newLine();
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
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
	
	
	private void writeTags(List<Tag> tags) {
		for (Tag tag : tags) {
			write(
				"        <tag k=\"" + escapeData(tag.getKey())
				+ "\" v=\"" + escapeData(tag.getValue()) + "\" />"
			);
			writeNewLine();
		}
	}
	
	
	private void writeSegmentReferences(List<SegmentReference> segRefs) {
		for (SegmentReference segRef : segRefs) {
			write(
				"        <seg id=\"" + segRef.getSegmentId() + "\" />"
			);
			writeNewLine();
		}
	}
	
	
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
	
	
	public void release() {
		// Do nothing.
	}
}
