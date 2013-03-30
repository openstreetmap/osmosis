// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;


/**
 * Provides common behaviour across all source element processors.
 * 
 * @author Brett Henderson
 */
public abstract class SourceElementProcessor extends BaseElementProcessor {
	private Sink sink;
	
	
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
	 */
	public SourceElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, enableDateParsing);
		
		this.sink = sink;
	}
	
	
	/**
	 * @return The sink.
	 */
	protected Sink getSink() {
		return sink;
	}
}
