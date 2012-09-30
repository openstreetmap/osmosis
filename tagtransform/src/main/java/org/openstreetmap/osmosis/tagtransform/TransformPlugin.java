// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


public class TransformPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		org.openstreetmap.osmosis.tagtransform.v0_6.TransformTaskFactory v0_6 =
			new org.openstreetmap.osmosis.tagtransform.v0_6.TransformTaskFactory();
		
		org.openstreetmap.osmosis.tagtransform.v0_6.TransformChangeTaskFactory change_v0_6 =
			new org.openstreetmap.osmosis.tagtransform.v0_6.TransformChangeTaskFactory();
		
		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();
		tasks.put("tag-transform-0.6", v0_6);
		tasks.put("tag-transform", v0_6);
		tasks.put("tt", v0_6);
		tasks.put("tag-transform-change-0.6", change_v0_6);
		tasks.put("tag-transform-change", change_v0_6);
		tasks.put("ttc", change_v0_6);
		return tasks;
	}

}
