package com.bretth.osm.conduit.xml.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osm.conduit.ConduitRuntimeException;


/**
 * Provides common functionality for all classes writing elements to xml.
 * 
 * @author Brett Henderson
 */
public class ElementWriter {
	
	/**
	 * The number of spaces to indent per indent level.
	 */
	private static final int INDENT_SPACES_PER_LEVEL = 4;
	
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
	
	
	private String elementName;
	private int indentLevel;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	protected ElementWriter(String elementName, int indentLevel) {
		this.elementName = elementName;
		this.indentLevel = indentLevel;
	}
	
	
	/**
	 * Writes a series of spaces to indent the current line.
	 * 
	 * @param writer
	 *            The underlying writer.
	 * @throws IOException
	 *             if an error occurs.
	 */
	private void writeIndent(BufferedWriter writer) throws IOException {
		int indentSpaceCount;
		
		indentSpaceCount = indentLevel * INDENT_SPACES_PER_LEVEL;
		
		for (int i = 0; i < indentSpaceCount; i++) {
			writer.append(' ');
		}
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
	protected String formatDate(Date date) {
		if (date != null) {
			// TODO: Complete data formatting.
			return date.toString();
		} else {
			return "";
		}
	}
	
	
	/**
	 * Writes an element opening line without the final closing portion of the
	 * tag.
	 * 
	 * @param writer
	 *            The underlying writer.
	 * @throws IOException
	 *             if an error occurs.
	 */
	protected void beginOpenElement(BufferedWriter writer) {
		try {
			writeIndent(writer);
			
			writer.append('<');
			writer.append(elementName);
		
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes out the opening tag of the element.
	 * 
	 * @param writer
	 *            The underlying writer.
	 * @param closeElement
	 *            If true, the element will be closed immediately and written as
	 *            a single tag in the output xml file.
	 */
	protected void endOpenElement(BufferedWriter writer, boolean closeElement) {
		try {
			writer.append('>');
			
			writer.newLine();
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Adds an attribute to the element.
	 * 
	 * @param writer
	 *            The underlying writer.
	 * @param name
	 *            The name of the attribute.
	 * @param value
	 *            The value of the attribute.
	 */
	protected void addAttribute(BufferedWriter writer, String name, String value) {
		try {
			writer.append(' ');
			writer.append(name);
			writer.append("=\"");
			
			writer.append(escapeData(value));
			
			writer.append('"');
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes the closing tag of the element.
	 * 
	 * @param writer
	 *            The underlying writer.
	 */
	protected void closeElement(BufferedWriter writer) {
		try {
			writeIndent(writer);
			
			writer.append("</");
			writer.append(elementName);
			writer.append('>');
			
			writer.newLine();
			
		} catch (IOException e) {
			throw new ConduitRuntimeException("Unable to write data.", e);
		}
	}
}
