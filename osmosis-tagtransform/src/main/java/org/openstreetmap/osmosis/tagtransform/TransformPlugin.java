// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.tagtransform.v0_6.TransformChangeTaskFactory;
import org.openstreetmap.osmosis.tagtransform.v0_6.TransformTaskFactory;


public class TransformPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		TransformTaskFactory transformFactory = new org.openstreetmap.osmosis.tagtransform.v0_6.TransformTaskFactory();

		TransformChangeTaskFactory changeTransformFactory =
				new org.openstreetmap.osmosis.tagtransform.v0_6.TransformChangeTaskFactory();

		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();
		tasks.put("tag-transform-0.6", transformFactory);
		tasks.put("tag-transform", transformFactory);
		tasks.put("tt", transformFactory);
		tasks.put("tag-transform-change-0.6", changeTransformFactory);
		tasks.put("tag-transform-change", changeTransformFactory);
		tasks.put("ttc", changeTransformFactory);
		return tasks;
	}
}
