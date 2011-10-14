// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.xml.common.ElementProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class is a SAX default handler for processing OSM Change XML files. It
 * utilises a tree of element processors to extract the data from the xml
 * structure.
 * 
 * @author Brett Henderson
 */
public class OsmChangeHandler extends DefaultHandler {
	private static final String ELEMENT_NAME_OSM_CHANGE = "osmChange";
	
	/**
	 * The root element processor used to process the osm change element.
	 */
	private ElementProcessor changeSourceElementProcessor;
	
	/**
	 * The currently active element processor. This is updated whenever new xml
	 * elements are entered or exited.
	 */
	private ElementProcessor elementProcessor;
	
	
	/**
	 * @param changeSink
	 *            The changeSink to write data to.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	public OsmChangeHandler(ChangeSink changeSink, boolean enableDateParsing) {
		changeSourceElementProcessor = new ChangeSourceElementProcessor(null, changeSink, enableDateParsing);
	}
	
	
	/**
	 * Begins processing of a new element.
	 * 
	 * @param uri
	 *            The uri.
	 * @param localName
	 *            The localName.
	 * @param qName
	 *            The qName.
	 * @param attributes
	 *            The attributes.
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		// Get the appropriate element processor for the element.
		if (elementProcessor != null) {
			// We already have an active element processor, therefore use the
			// active element processor to retrieve the appropriate child
			// element processor.
			elementProcessor = elementProcessor.getChild(uri, localName, qName);
		} else if (ELEMENT_NAME_OSM_CHANGE.equals(qName)) {
			// There is no active element processor which means we have
			// encountered the root osm element.
			elementProcessor = changeSourceElementProcessor;
		} else {
			// There is no active element processor which means that this is a
			// root element. The root element in this case does not match the
			// expected name.
			throw new OsmosisRuntimeException("This does not appear to be an OSM Change XML file.");
		}
		
		// Initialise the element processor with the attributes of the new element.
		elementProcessor.begin(attributes);
	}
	
	
	/**
	 * Ends processing of the current element.
	 * 
	 * @param uri
	 *            The uri.
	 * @param localName
	 *            The localName.
	 * @param qName
	 *            The qName.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		// Tell the currently active element processor to complete its processing.
		elementProcessor.end();
		
		// Set the active element processor to the parent of the existing processor.
		elementProcessor = elementProcessor.getParent();
	}
}
