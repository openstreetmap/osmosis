package com.bretth.osm.conduit.change.impl;


/**
 * Defines the various states an input source to a change function can be in.
 * 
 * @author Brett Henderson
 */
public enum InputStatus {
	/**
	 * No data has been received from the source.
	 */
	NOT_STARTED(),
	/**
	 * The last data received was a node.
	 */
	NODE_STAGE(),
	/**
	 * The last data received was a segment.
	 */
	SEGMENT_STAGE(),
	/**
	 * The last data received was a way.
	 */
	WAY_STAGE(),
	/**
	 * The source has been completed sending all data.
	 */
	COMPLETE();
};
