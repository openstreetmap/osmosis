// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.migrate;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * The task manager factory for a 0.5 to 0.6 migration task.
 *
 * @author Brett Henderson
 */
public class MigrateV05ToV06Factory extends TaskManagerFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new Sink05Source06Manager(
			taskConfig.getId(),
			new MigrateV05ToV06(),
			taskConfig.getPipeArgs()
		);
	}
}
