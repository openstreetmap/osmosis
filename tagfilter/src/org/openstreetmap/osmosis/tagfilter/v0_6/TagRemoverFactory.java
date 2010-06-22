// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with drop tags task
 * specific common methods.
 *
 * @author Jochen Topf
 */
public class TagRemoverFactory extends TaskManagerFactory {
	/**
     * {@inheritDoc}
     */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String keys;
		String keyPrefixes;
		
		try {
			keys = getStringArgument(taskConfig, "keys");
		} catch (OsmosisRuntimeException e) {
			keys = "";
		}
		try {
			keyPrefixes = getStringArgument(taskConfig, "keyPrefixes");
		} catch (OsmosisRuntimeException e) {
			keyPrefixes = "";
		}
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new TagRemover(keys, keyPrefixes),
			taskConfig.getPipeArgs()
		);
	}
}
