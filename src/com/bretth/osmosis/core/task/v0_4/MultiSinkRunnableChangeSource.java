package com.bretth.osmosis.core.task.v0_4;



/**
 * Defines the interface for combining multi sink and change source
 * functionality. This is typically used by classes performing difference
 * analysis of two data sources.
 * 
 * @author Brett Henderson
 */
public interface MultiSinkRunnableChangeSource extends MultiSink, RunnableChangeSource {
	// Interface only combines functionality of its extended interfaces.
}
