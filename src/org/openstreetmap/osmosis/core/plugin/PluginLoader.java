// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.plugin;

import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;


/**
 * Defines the methods required by a plugin loading implementation. The plugin
 * loader is used to define the tasks provided by a plugin.
 * 
 * @author Brett Henderson
 */
public interface PluginLoader {
	/**
	 * Loads all task factories provided by the plugin and specifies the names
	 * to register them under.
	 * 
	 * @return A map between task names and their factory implementations.
	 */
	Map<String, TaskManagerFactory> loadTaskFactories();
}
