package com.bretth.osmosis.core.customdb;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.task.v0_5.DatasetSink;
import com.bretth.osmosis.core.task.v0_5.SinkDatasetSource;


/**
 * Receives input data as a stream and builds a dataset containing all of the
 * data. The dataset is then passed to the downstream task.
 * 
 * @author Brett Henderson
 */
public class WriteDatasetAndForward implements SinkDatasetSource {
	
	/**
	 * {@inheritDoc}
	 */
	public void setDatasetSink(DatasetSink datasetSink) {
		// TODO: Finish this method.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// TODO: Finish this method.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		// TODO: Finish this method.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		// TODO: Finish this method.
	}
}
