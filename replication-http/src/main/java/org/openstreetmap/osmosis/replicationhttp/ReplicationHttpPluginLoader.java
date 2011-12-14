// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * The plugin loader for the API Schema tasks.
 * 
 * @author Brett Henderson
 */
public class ReplicationHttpPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		return factoryMap;
	}
}
