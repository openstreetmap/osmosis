// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with used-node filter task
 * specific common methods.
 *
 * @author Brett Henderson
 * @author Christoph Sommer
 */
public class NodeKeyValueFilterFactory extends TaskManagerFactory {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String keyValueList = getStringArgument(taskConfig, "keyValueList");
		return new SinkSourceManager(
			taskConfig.getId(),
			new NodeKeyValueFilter(keyValueList),
			taskConfig.getPipeArgs()
		);
	}

}
