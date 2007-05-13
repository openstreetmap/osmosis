package com.bretth.osm.conduit.task;


/**
 * Defines the interface for all tasks performing some form of transformation on
 * an input source before sending on to an output sink. This includes filtering
 * tasks and modification tasks.
 * 
 * @author Brett Henderson
 */
public interface OsmTransformer extends OsmSink, OsmSource {
	// Interface only combines functionality of its extended interfaces.
}
