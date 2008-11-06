// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;

import com.bretth.osmosis.core.container.v0_5.Dataset;
import com.bretth.osmosis.core.lifecycle.Releasable;
import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks consuming datasets.
 * 
 * @author Brett Henderson
 */
public interface DatasetSink extends Task, Releasable {
	
	/**
	 * Process the dataset. This must only be called once. This will perform all
	 * finalisation tasks such as database commits as necessary to complete the
	 * task.
	 * 
	 * @param dataset
	 *            The dataset to be processed.
	 */
	public void process(Dataset dataset);
}
