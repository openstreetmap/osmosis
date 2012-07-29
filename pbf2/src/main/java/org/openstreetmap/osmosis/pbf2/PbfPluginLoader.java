// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReaderFactory;


/**
 * The plugin loader for the PBF2 tasks.
 * 
 * @author Brett Henderson
 */
public class PbfPluginLoader implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;

		PbfReaderFactory reader = new PbfReaderFactory();

		factoryMap = new HashMap<String, TaskManagerFactory>();
		factoryMap.put("read-pbf-fast", reader);
		factoryMap.put("rbf", reader);

		factoryMap.put("read-pbf-fast-0.6", reader);

		return factoryMap;
	}
}
