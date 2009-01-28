// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;


/**
 * Defines the interface for combining sink and dataset source functionality. This is
 * typically implemented by classes converting a stream of data into an indexed random access dataset.
 * 
 * @author Brett Henderson
 */
public interface SinkDatasetSource extends Sink, DatasetSource {
	// Interface only combines functionality of its extended interfaces.
}
