// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkSourceManager;


/**
 * The task manager factory for a change to full-history convertor.
 * 
 * @author Brett Henderson
 */
public class ChangeToFullHistoryConvertorFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSinkSourceManager(
			taskConfig.getId(),
			new ChangeToFullHistoryConvertor(),
			taskConfig.getPipeArgs()
		);
	}
}
