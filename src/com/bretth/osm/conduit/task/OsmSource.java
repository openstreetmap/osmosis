package com.bretth.osm.conduit.task;


/**
 * Defines the interface for tasks producing OSM data types.
 * 
 * @author Brett Henderson
 */
public interface OsmSource extends Task {
	
	/**
	 * Sets the osm sink to send data to.
	 * 
	 * @param osmSink
	 *            The sink for receiving all produced data.
	 */
	void setOsmSink(OsmSink osmSink);
}
