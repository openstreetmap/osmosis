package com.bretth.osmosis.core.task;


/**
 * Defines the interface for all tasks producing OSM changes to data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSource extends Task {
	
	/**
	 * Sets the change sink to send data to.
	 * 
	 * @param changeSink
	 *            The sink for receiving all produced data.
	 */
	void setChangeSink(ChangeSink changeSink);
}
