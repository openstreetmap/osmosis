// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.logging.Logger;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for an osm element.
 * 
 * @author Brett Henderson
 */
public class OsmElementProcessor extends SourceElementProcessor {
	
	private static final Logger LOG = Logger.getLogger(OsmElementProcessor.class.getName());
	
	private static final String ELEMENT_NAME_BOUND_LEGACY = "bound";
	private static final String ELEMENT_NAME_BOUNDS = "bounds";
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ELEMENT_NAME_RELATION = "relation";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	private static final String ATTRIBUTE_NAME_GENERATOR = "generator";
	
	private NodeElementProcessor nodeElementProcessor;
	private WayElementProcessor wayElementProcessor;
	private RelationElementProcessor relationElementProcessor;
	
	private boolean foundBound = false;
	private boolean foundEntities = false;
	private boolean validateVersion;

	private String generator;
	

	/**
	 * Creates a new instance (coordinates are required).
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 * @param validateVersion If true, a version attribute will be checked and validated.
	 */
	public OsmElementProcessor(
			BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing, boolean validateVersion) {
		this(parentProcessor, sink, enableDateParsing, validateVersion, true);
	}
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 * @param validateVersion If true, a version attribute will be checked and validated.
	 * @param coordinatesRequired
	 *            If true, nodes without lat and lon attributes set will cause an exception.
	 */
	public OsmElementProcessor(
			BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing, boolean validateVersion,
			boolean coordinatesRequired) {
		super(parentProcessor, sink, enableDateParsing);
		
		this.validateVersion = validateVersion;

		nodeElementProcessor = new NodeElementProcessor(this, getSink(), enableDateParsing, coordinatesRequired);
		wayElementProcessor = new WayElementProcessor(this, getSink(), enableDateParsing);
		relationElementProcessor = new RelationElementProcessor(this, getSink(), enableDateParsing);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		if (validateVersion) {
			String fileVersion;
			
			fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
			
			if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
				LOG.warning(
					"Expected version " + XmlConstants.OSM_VERSION
					+ " but received " + fileVersion + "."
				);
			}
		}
		
		generator = attributes.getValue(ATTRIBUTE_NAME_GENERATOR);
	}
	
	
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
	@Override
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_BOUNDS.equals(qName) || ELEMENT_NAME_BOUND_LEGACY.equals(qName)) {
			if (foundEntities) {
				throw new OsmosisRuntimeException("Bound element must come before any entities.");
			}
			if (foundBound) {
				throw new OsmosisRuntimeException("Only one bound element allowed.");
			}
			foundBound = true;
			if (ELEMENT_NAME_BOUND_LEGACY.equals(qName)) {
				LOG.fine("Legacy <bound> element encountered.");
				return new LegacyBoundElementProcessor(this, getSink(), true);
			} else {
				return new BoundsElementProcessor(this, getSink(), true, generator);
			}
		} else if (ELEMENT_NAME_NODE.equals(qName)) {
			foundEntities = true;
			return nodeElementProcessor;
		} else if (ELEMENT_NAME_WAY.equals(qName)) {
			foundEntities = true;
			return wayElementProcessor;
		} else if (ELEMENT_NAME_RELATION.equals(qName)) {
			foundEntities = true;
			return relationElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		// This class produces no data and therefore doesn't need to do anything
		// when the end of the element is reached.
	}
}
