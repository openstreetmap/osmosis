// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with  way tag filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 * @author Christoph Sommer
 * @author David Turner
 */
public class WayTagFilterFactory extends TaskManagerFactory {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String keyList = getStringArgument(
				taskConfig, "keyList", "highway,maxspeed,access,psv,bus,name,alt_name,name_1,oneway,layer,bridge,tunnel,junction,surface");
		return new SinkSourceManager(
			taskConfig.getId(),
			new WayTagFilter(keyList),
			taskConfig.getPipeArgs()
		);
	}

}
