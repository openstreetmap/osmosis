// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.DatasetSinkSourceManager;


/**
 * The task manager factory for reading the entire contents of a dataset.
 * 
 * @author Brett Henderson
 */
public class DumpDatasetFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new DatasetSinkSourceManager(
			taskConfig.getId(),
			new DumpDataset(),
			taskConfig.getPipeArgs()
		);
	}
}
