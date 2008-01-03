// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
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
	 * Process the dataset. This must only be called once. This will perform all
	 * finalisation tasks such as database commits as necessary to complete the
	 * task.
	 * 
	 * @param dataset
	 *            The dataset to be processed.
	 */
	public void process(Dataset dataset);
	
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete. It
	 * should be called within a finally block to ensure it is called in
	 * exception scenarios. Chained implementations will call their output
	 * sinks.
	 */
	public void release();
}
