// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


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
