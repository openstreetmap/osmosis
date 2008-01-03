// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5.impl;

import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.xml.common.BaseElementProcessor;


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
