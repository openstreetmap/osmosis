package com.bretth.osmosis.core.migrate;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * The task manager factory for a 0.5 to 0.6 change migration task.
 *
 * @author Brett Henderson
 */
public class MigrateChangeV05ToV06Factory extends TaskManagerFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new ChangeSink05ChangeSource06Manager(
			taskConfig.getId(),
			new MigrateChangeV05ToV06(),
			taskConfig.getPipeArgs()
		);
	}
}
