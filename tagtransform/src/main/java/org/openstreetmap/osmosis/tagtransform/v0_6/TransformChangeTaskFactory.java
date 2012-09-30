// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkChangeSourceManager;


public class TransformChangeTaskFactory extends TaskManagerFactory {

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String configFile =
				getStringArgument(taskConfig, "file", getDefaultStringArgument(taskConfig, "transform.xml"));
		String statsFile = getStringArgument(taskConfig, "stats", null);
		return new ChangeSinkChangeSourceManager(taskConfig.getId(), new TransformChangeTask(configFile, statsFile),
				taskConfig.getPipeArgs());
	}

}
