// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import org.xml.sax.Attributes;


/**
 * An element processor provides a handler for processing a specific xml element
 * within a document. It provides a state pattern approach to processing nested
 * xml structures.
 * 
 * @author Brett Henderson
 */
public interface ElementProcessor {
	/**
	 * Initialises the element processor with attributes for a new element to be
	 * processed.
	 * 
	 * @param attributes
	 *            The attributes of the new element.
	 */
	void begin(Attributes attributes);
	
	/**
	 * Retrieves the appropriate child element processor for the newly
	 * encountered nested element.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return The appropriate element processor for the nested element.
	 */
	ElementProcessor getChild(String uri, String localName, String qName);
	
	/**
	 * Returns the parent element processor.
	 * 
	 * @return The parent element processor.
	 */
	ElementProcessor getParent();
	
	/**
	 * Finalises processing for the element processor, this is called when the
	 * end of an element is reached.
	 */
	void end();
}
