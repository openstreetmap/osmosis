// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;


/**
 * Defines the interface for combining dataset sink and source functionality.
 * This is typically used by classes performing some data extraction from a
 * dataset and producing a subset output.
 * 
 * @author Brett Henderson
 */
public interface DatasetSinkSource extends DatasetSink, Source {
	// Interface only combines functionality of its extended interfaces.
}
