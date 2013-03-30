// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;

/**
 * The task manager factory for a flatten/simplify filter.
 */
public class FlattenFilterFactory extends TaskManagerFactory {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(
			TaskConfiguration taskConfig) {
		return new SinkSourceManager(
			taskConfig.getId(),
			new FlattenFilter(),
			taskConfig.getPipeArgs()
		);
	}
}
