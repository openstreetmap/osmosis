package com.bretth.osm.conduit.sort.impl;


/**
 * A simple sink for receiving data produced by the 
 * @author Brett Henderson
 *
 * @param <DataType>
 */
public interface SortSink<DataType> {
	/**
	 * Process the data.
	 * 
	 * @param data
	 *            The result data.
	 */
	public void processData(DataType data);
}
