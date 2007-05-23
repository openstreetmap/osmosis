package com.bretth.osm.conduit.task;


/**
 * Extends the basic OsmSource interface with the Runnable capability. Runnable
 * is not applied to the OsmSource interface because tasks that act as filters
 * do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableSource extends Source, Runnable {
	// This interface combines OsmSource and Runnable but doesn't introduce
	// methods of its own.
}
