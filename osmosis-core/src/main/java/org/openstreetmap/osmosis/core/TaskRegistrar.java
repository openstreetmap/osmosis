// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactoryRegister;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * Provides the initialisation logic for registering all task factories.
 *
 * @author Brett Henderson
 */
public class TaskRegistrar {

    /**
     * Our logger for debug and error -output.
     */
    private static final Logger LOG = Logger.getLogger(TaskRegistrar.class.getName());

	/**
	 * The register containing all known task manager factories.
	 */
	private TaskManagerFactoryRegister factoryRegister;


	/**
	 * Creates a new instance.
	 */
	public TaskRegistrar() {
		factoryRegister = new TaskManagerFactoryRegister();
	}


	/**
	 * Returns the configured task manager factory register configured.
	 * 
	 * @return The task manager factory register.
	 */
	public TaskManagerFactoryRegister getFactoryRegister() {
		return factoryRegister;
	}


	/**
	 * Initialises factories for all tasks. Loads additionally specified plugins
	 * as well as default tasks.
	 * 
	 * @param plugins
	 *            The class names of all plugins to be loaded.
	 */
	public void initialize(List<String> plugins) {
		// Register the built-in plugins.
		loadBuiltInPlugins();

		// Register the plugins specified on the command line.
		for (String plugin : plugins) {
			loadPlugin(plugin);
		}
	}

	/**
	 * Loads a plugin manually.
	 * 
	 * @param pluginLoader The pluginLoader that you wish to load.
	 */
	public void loadPlugin(final PluginLoader pluginLoader) {
		final Map<String, TaskManagerFactory> pluginTasks = pluginLoader.loadTaskFactories();
		// register the plugin tasks
		pluginTasks.entrySet().forEach(task -> {
			if (!this.factoryRegister.containsTaskType(task.getKey())) {
				this.factoryRegister.register(task.getKey(), task.getValue());
			}
		});
	}

	private void loadBuiltInPlugins() {
		final String pluginResourceName = "osmosis-plugins.conf";
		
		try {
			for (URL pluginConfigurationUrl : Collections.list(Thread.currentThread()
					.getContextClassLoader().getResources(pluginResourceName))) {
				BufferedReader pluginReader;
				
				LOG.finer("Loading plugin configuration file from url " + pluginConfigurationUrl + ".");
				
				try (InputStream pluginInputStream = pluginConfigurationUrl.openStream()) {
					if (pluginInputStream == null) {
						throw new OsmosisRuntimeException("Cannot open URL " + pluginConfigurationUrl + ".");
					}
					
					pluginReader = new BufferedReader(new InputStreamReader(pluginInputStream));
					
					for (;;) {
						String plugin;
						
						plugin = pluginReader.readLine();
						if (plugin == null) {
							break;
						}
						
						plugin = plugin.trim();
						if (!plugin.isEmpty()) {
							LOG.finer("Loading plugin via loader " + plugin + ".");
							
							loadPlugin(plugin);
						}
					}
				}
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
					"Unable to load the plugins based on " + pluginResourceName
							+ " resources.");
		}
	}


	/**
	 * Loads the tasks associated with a plugin (old plugin-api).
	 * 
	 * @param plugin
	 *            The plugin loader class name.
	 */
	private void loadPlugin(final String plugin) {
		ClassLoader classLoader;

		// Obtain the thread context class loader. This becomes important if run
		// within an application server environment where plugins might be
		// inaccessible to this class's classloader.
		classLoader = Thread.currentThread().getContextClassLoader();

		loadPluginClass(plugin, classLoader);
	}


	/**
	 * Load the given plugin, old API or new JPF.
	 * 
	 * @param pluginClassName
	 *            the name of the class to instantiate
	 * @param classLoader
	 *            the ClassLoader to use
	 */
	@SuppressWarnings("unchecked")
	private void loadPluginClass(final String pluginClassName, final ClassLoader classLoader) {
		Class<?> untypedPluginClass;
		PluginLoader pluginLoader;
		Map<String, TaskManagerFactory> pluginTasks;
		// Load the plugin class.
		try {
			untypedPluginClass = classLoader.loadClass(pluginClassName);
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to load plugin class (" + pluginClassName + ").", e);
		}
		// Verify that the plugin implements the plugin loader interface.
		if (!PluginLoader.class.isAssignableFrom(untypedPluginClass)) {
			throw new OsmosisRuntimeException("The class (" + pluginClassName + ") does not implement interface ("
					+ PluginLoader.class.getName() + "). Maybe it's not a plugin?");
		}
		Class<PluginLoader> pluginClass = (Class<PluginLoader>) untypedPluginClass;

		// Instantiate the plugin loader.
		try {
			pluginLoader = pluginClass.getDeclaredConstructor().newInstance();
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to instantiate plugin class (" + pluginClassName + ").", e);
		}

		// Obtain the plugin task factories with their names.
		pluginTasks = pluginLoader.loadTaskFactories();

		// Register the plugin tasks.
		for (Entry<String, TaskManagerFactory> taskEntry : pluginTasks.entrySet()) {
			factoryRegister.register(taskEntry.getKey(), taskEntry.getValue());
		}
	}
}
