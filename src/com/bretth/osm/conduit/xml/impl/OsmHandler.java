package com.bretth.osm.conduit.xml.impl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.Sink;


/**
 * This class is a SAX default handler for processing OSM XML files. It utilises
 * a tree of element processors to extract the data from the xml structure.
 * 
 * @author Brett Henderson
 */
public class OsmHandler extends DefaultHandler {
	private static final String ELEMENT_NAME_OSM = "osm";
	
	/**
	 * The root element processor used to process the osm element.
	 */
	private ElementProcessor osmElementProcessor;
	
	/**
	 * The currently active element processor. This is updated whenever new xml
	 * elements are entered or exited.
	 */
	private ElementProcessor elementProcessor;
	
	
	/**
	 * @param osmSink The new osmSink to write data to.
	 */
	public OsmHandler(Sink osmSink) {
		osmElementProcessor = new OsmElementProcessor(null, osmSink);
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
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Get the appropriate element processor for the element.
		if (elementProcessor != null) {
			// We already have an active element processor, therefore use the
			// active element processor to retrieve the appropriate child
			// element processor.
			elementProcessor = elementProcessor.getChild(uri, localName, qName);
		} else if (ELEMENT_NAME_OSM.equals(qName)) {
			// There is no active element processor which means we have
			// encountered the root osm element.
			elementProcessor = osmElementProcessor;
		} else {
			// There is no active element processor which means that this is a
			// root element. The root element in this case does not match the
			// expected name.
			throw new ConduitRuntimeException("This does not appear to be an OSM XML file.");
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
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Tell the currently active element processor to complete its processing.
		elementProcessor.end();
		
		// Set the active element processor to the parent of the existing processor.
		elementProcessor = elementProcessor.getParent();
	}
}
