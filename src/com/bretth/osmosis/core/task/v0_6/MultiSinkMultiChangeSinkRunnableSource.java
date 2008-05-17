// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_6;


/**
 * Defines the interface for combining sink, change sink and source
 * functionality. This is typically used by classes adding a change set to an
 * input to produce a new output.
 * 
 * @author Brett Henderson
 */
public interface MultiSinkMultiChangeSinkRunnableSource extends MultiSink, MultiChangeSink, RunnableSource {
	// Interface only combines functionality of its extended interfaces.
}
