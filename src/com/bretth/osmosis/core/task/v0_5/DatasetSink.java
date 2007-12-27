package com.bretth.osmosis.core.task.v0_5;

import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks consuming datasets.
 * 
 * @author Brett Henderson
 */
public interface DatasetSink extends Task {
	
	/**
	 * Process the dataset.
	 * 
	 * @param dataset
	 *            The dataset to be processed.
	 */
	public void process(Dataset dataset);
	
	/**
	 * Performs finalisation tasks such as database commits as necessary to
	 * complete the task. Must be called by clients when all objects have been
	 * processed. It should not be called in exception scenarios. Chained
	 * implementations will call their output sinks.
	 */
	public void complete();
	
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete. It
	 * should be called within a finally block to ensure it is called in
	 * exception scenarios. Chained implementations will call their output
	 * sinks.
	 */
	public void release();
}
