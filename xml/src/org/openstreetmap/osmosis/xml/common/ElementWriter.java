// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import java.io.IOException;
import java.io.Writer;
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
     * Defines the characters that must be replaced by
     * an encoded string when writing to XML.
     */
    private static final Map<Character, String> XML_ENCODING;

    static {
        // Define all the characters and their encodings.
        XML_ENCODING = new HashMap<Character, String>();
        
        // Non-xml compatible control characters will not be written
        // with the exception of tab, carriage return and line feed.
        for (int i = 0; i <= 0x1F; i++) {
        	if (i != 0x9 && i != 0xA && i != 0xD) {
        		XML_ENCODING.put(new Character((char) i), "");
        	}
        }
        XML_ENCODING.put(new Character((char) 0x7F), "");
        
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
    private Writer myWriter;

    /**
     * The name of the element to be written.
     */
    private final String myElementName;

    /**
     * The indent level of the element.
     */
    private final int myIndentLevel;

    private final TimestampFormat myTimestampFormat;

    /**
     * Line separator string.  This is the value of the line.separator
     * property at the moment that the stream was created.
     */
    private String myLineSeparator;

    /**
     * Creates a new instance.
     *
     * @param anElementName The name of the element to be written.
     * @param anIndentionLevel The indent level of the element.
     */
    protected ElementWriter(final String anElementName,
                            final int anIndentionLevel) {
        this.myElementName = anElementName;
        this.myIndentLevel = anIndentionLevel;

        myTimestampFormat = new XmlTimestampFormat();
        this.myLineSeparator = System.getProperty("line.separator");
    }

    /**
     * Sets the writer used as the xml output destination.
     *
     * @param aWriter The writer.
     */
    public void setWriter(final Writer aWriter) {
    	if (aWriter == null) {
    		throw new IllegalArgumentException("null writer given");
    	}
        this.myWriter = aWriter;
    }

    /**
     * Writes a series of spaces to indent the current line.
     *
     * @throws IOException if an error occurs.
     */
    private void writeIndent() throws IOException {
        int indentSpaceCount;

        indentSpaceCount = myIndentLevel * INDENT_SPACES_PER_LEVEL;

        for (int i = 0; i < indentSpaceCount; i++) {
            myWriter.append(' ');
        }
    }

    /**
     * A utility method for encoding data in XML format.
     *
     * @param data The data to be formatted.
     * @return The formatted data. This may be the input
     *         string if no changes are required.
     */
    private String escapeData(final String data) {
        StringBuilder buffer = null;

        for (int i = 0; i < data.length(); ++i) {
            char currentChar = data.charAt(i);

            String replacement = XML_ENCODING.get(new Character(currentChar));

            if (replacement != null) {
                if (buffer == null) {
                    buffer = new StringBuilder(data.substring(0, i));
                }
                buffer.append(replacement);

            } else if (buffer != null) {
                buffer.append(currentChar);
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
        return myTimestampFormat;
    }

    /**
     * Writes an element opening line without the final
     * closing portion of the tag.
     */
    protected void beginOpenElement() {
        try {
            writeIndent();

            myWriter.append('<');
            myWriter.append(this.myElementName);

        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to write data.", e);
        }
    }

    /**
     * Writes out the opening tag of the element.
     *
     * @param closeElement If true, the element will be closed
     *        immediately and written as a single
     *        tag in the output xml file.
     */
    protected void endOpenElement(final boolean closeElement) {
        try {
            if (closeElement) {
                myWriter.append('/');
            }
            myWriter.append('>');

            myWriter.append(this.myLineSeparator);

        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to write data.", e);
        }
    }

    /**
     * Adds an attribute to the element.
     *
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     */
    protected void addAttribute(final String name, final String value) {
        try {
            myWriter.append(' ');
            myWriter.append(name);
            myWriter.append("=\"");

            myWriter.append(escapeData(value));

            myWriter.append('"');

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

            myWriter.append("</");
            myWriter.append(myElementName);
            myWriter.append('>');

            myWriter.append(this.myLineSeparator);

        } catch (IOException e) {
            throw new OsmosisRuntimeException("Unable to write data.", e);
        }
    }
}
