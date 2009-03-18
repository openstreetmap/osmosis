// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;


/**
 * Provides common functionality for all classes writing elements to xml.
 * 
 * @author Brett Henderson
 */
public class ElementWriter {
	
	/**
	 * The number of spaces to indent per indent level.
	 */
	private static final int INDENT_SPACES_PER_LEVEL = 2;
	
	/**
	 * Defines the characters that must be replaced by an encoded string when writing to XML.
	 */
	private static final Map<Character, String> XML_ENCODING;
	
	static {
		// Define all the characters and their encodings.
		XML_ENCODING = new HashMap<Character, String>();
		
		XML_ENCODING.put(new Character('<'), "&lt;");
		XML_ENCODING.put(new Character('>'), "&gt;");
		XML_ENCODING.put(new Character('"'), "&quot;");
		XML_ENCODING.put(new Character('\''), "&apos;");
		XML_ENCODING.put(new Character('&'), "&amp;");
		XML_ENCODING.put(new Character('\n'), "&#xA;");
		XML_ENCODING.put(new Character('\r'), "&#xD;");
		XML_ENCODING.put(new Character('\t'), "&#x9;");
	}
	
	
	/**
	 * The output destination for writing all xml.
	 */
	protected BufferedWriter writer;
	private String elementName;
	private int indentLevel;
	private TimestampFormat timestampFormat;
	
	
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
		
		timestampFormat = new XmlTimestampFormat();
	}
	
	
	/**
	 * Sets the writer used as the xml output destination.
	 * 
	 * @param writer
	 *            The writer.
	 */
	public void setWriter(BufferedWriter writer) {
		this.writer = writer;
	}
	
	
	/**
	 * Writes a series of spaces to indent the current line.
	 * 
	 * @param writer
	 *            The underlying writer.
	 * @throws IOException
	 *             if an error occurs.
	 */
	private void writeIndent() throws IOException {
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
			String replacement = XML_ENCODING.get(new Character(data.charAt(i)));
			
			if (replacement != null) {
				if (buffer == null) {
					buffer = new StringBuffer(data.substring(0, i));
				}
				buffer.append(replacement);
				
			} else if (buffer != null) {
				buffer.append(data.charAt(i));
			}
		}
		
		if (buffer == null) {
			return data;
		} else {
			return buffer.toString();
		}
	}
	
	
	/**
	 * Returns a timestamp format suitable for xml files.
	 * 
	 * @return The timestamp format.
	 */
	protected TimestampFormat getTimestampFormat() {
		return timestampFormat;
	}
	
	
	/**
	 * Writes an element opening line without the final closing portion of the
	 * tag.
	 */
	protected void beginOpenElement() {
		try {
			writeIndent();
			
			writer.append('<');
			writer.append(elementName);
		
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes out the opening tag of the element.
	 * 
	 * @param closeElement
	 *            If true, the element will be closed immediately and written as
	 *            a single tag in the output xml file.
	 */
	protected void endOpenElement(boolean closeElement) {
		try {
			if (closeElement) {
				writer.append('/');
			}
			writer.append('>');
			
			writer.newLine();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Adds an attribute to the element.
	 * 
	 * @param name
	 *            The name of the attribute.
	 * @param value
	 *            The value of the attribute.
	 */
	protected void addAttribute(String name, String value) {
		try {
			writer.append(' ');
			writer.append(name);
			writer.append("=\"");
			
			writer.append(escapeData(value));
			
			writer.append('"');
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
	
	
	/**
	 * Writes the closing tag of the element.
	 */
	protected void closeElement() {
		try {
			writeIndent();
			
			writer.append("</");
			writer.append(elementName);
			writer.append('>');
			
			writer.newLine();
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write data.", e);
		}
	}
}
