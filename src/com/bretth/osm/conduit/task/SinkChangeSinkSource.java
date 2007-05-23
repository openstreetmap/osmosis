package com.bretth.osm.conduit.task;


/**
 * Defines the interface for combining sink, change sink and source
 * functionality. This is typically used by classes adding a change set to an
 * input to produce a new output.
 * 
 * @author Brett Henderson
 */
public interface SinkChangeSinkSource extends MultiSink, MultiChangeSink, Source {
	// Interface only combines functionality of its extended interfaces.
}
