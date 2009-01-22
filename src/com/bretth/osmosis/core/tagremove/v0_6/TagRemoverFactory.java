// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.tagremove.v0_6;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


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
		} catch (OsmosisRuntimeException e){
			keyPrefixes = "";
		}
		
		return new SinkSourceManager(
			taskConfig.getId(),
			new TagRemover(keys, keyPrefixes),
			taskConfig.getPipeArgs()
		);
	}
}
