// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.misc.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;


/**
 * The task manager factory for a null writer.
 * 
 * @author Brett Henderson
 */
public class NullWriterFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new SinkManager(taskConfig.getId(), new NullWriter(), taskConfig.getPipeArgs());
	}
}
