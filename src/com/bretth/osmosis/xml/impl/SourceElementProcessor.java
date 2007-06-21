package com.bretth.osmosis.xml.impl;

import com.bretth.osmosis.task.Sink;


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
	 */
	public SourceElementProcessor(BaseElementProcessor parentProcessor, Sink sink) {
		super(parentProcessor);
		
		this.sink = sink;
	}
	
	
	/**
	 * @return The sink.
	 */
	protected Sink getSink() {
		return sink;
	}
}
