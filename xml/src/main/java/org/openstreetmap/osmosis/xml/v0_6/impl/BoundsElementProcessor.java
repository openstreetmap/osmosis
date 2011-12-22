// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;

/**
 * Provides an element processor implementation for a node.
 * 
 * @author Karl Newman
 * @author Igor Podolskiy
 */
public class BoundsElementProcessor extends SourceElementProcessor {

	private Bound bound;
	private String defaultOrigin;


	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current date will be used
	 *            thus saving parsing time.
	 * @param defaultOrigin
	 * 			  The origin to set on the bound entity if there is no explicit origin defined.
	 */
	public BoundsElementProcessor(BaseElementProcessor parentProcessor,
	        Sink sink,
	        boolean enableDateParsing,
	        String defaultOrigin) {
		super(parentProcessor, sink, enableDateParsing);
		this.defaultOrigin = defaultOrigin;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void begin(Attributes attributes) {
		double bottom = getRequiredDoubleValue(attributes, XmlConstants.ATTRIBUTE_NAME_MINLAT);
		double left = getRequiredDoubleValue(attributes, XmlConstants.ATTRIBUTE_NAME_MINLON);
		double top = getRequiredDoubleValue(attributes, XmlConstants.ATTRIBUTE_NAME_MAXLAT);
		double right = getRequiredDoubleValue(attributes, XmlConstants.ATTRIBUTE_NAME_MAXLON);

		String origin = attributes.getValue(XmlConstants.ATTRIBUTE_NAME_ORIGIN);
		if (origin == null) {
			origin = defaultOrigin;
		}
		bound = new Bound(right, left, top, bottom, origin);
	}

	private double getRequiredDoubleValue(Attributes attributes, String attributeName) {
		String valueString = attributes.getValue(attributeName);

		if (valueString == null) {
			throw new OsmosisRuntimeException(String.format(
					"Required attribute %s of the bounds element is missing", attributeName));
		}
		try {
			return Double.parseDouble(valueString);
		} catch (NumberFormatException e) {
			throw new OsmosisRuntimeException(
					String.format("Cannot parse the %s attribute of the bounds element", attributeName), 
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void end() {
		getSink().process(new BoundContainer(bound));
		bound = null;
	}

}
