// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.ManifestProcessingException;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;
import org.openstreetmap.osmosis.core.buffer.v0_6.ChangeBufferFactory;
import org.openstreetmap.osmosis.core.buffer.v0_6.EntityBufferFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullChangeWriterFactory;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactoryRegister;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;
import org.openstreetmap.osmosis.core.progress.v0_6.ChangeProgressLoggerFactory;
import org.openstreetmap.osmosis.core.progress.v0_6.EntityProgressLoggerFactory;
import org.openstreetmap.osmosis.core.report.v0_6.EntityReporterFactory;
import org.openstreetmap.osmosis.core.report.v0_6.IntegrityReporterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForSeekableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeSorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.ChangeTagSorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntitySorterFactory;
import org.openstreetmap.osmosis.core.sort.v0_6.TagSorterFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.ChangeTeeFactory;
import org.openstreetmap.osmosis.core.tee.v0_6.EntityTeeFactory;


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
		EntitySorterFactory entitySorterFactory06;
		ChangeSorterFactory changeSorterFactory06;

		// Configure factories that require additional information.
		entitySorterFactory06 = new EntitySorterFactory();
		entitySorterFactory06.registerComparator("TypeThenId", new EntityContainerComparator(
				new EntityByTypeThenIdComparator()), true);
		changeSorterFactory06 = new ChangeSorterFactory();
		changeSorterFactory06.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory06.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);

		// Register factories.
		factoryRegister.register("sort", entitySorterFactory06);
		factoryRegister.register("s", entitySorterFactory06);
		factoryRegister.register("sort-change", changeSorterFactory06);
		factoryRegister.register("sc", changeSorterFactory06);
		factoryRegister.register("write-null", new NullWriterFactory());
		factoryRegister.register("wn", new NullWriterFactory());
		factoryRegister.register("write-null-change", new NullChangeWriterFactory());
		factoryRegister.register("wnc", new NullChangeWriterFactory());
		factoryRegister.register("buffer", new EntityBufferFactory());
		factoryRegister.register("b", new EntityBufferFactory());
		factoryRegister.register("buffer-change", new ChangeBufferFactory());
		factoryRegister.register("bc", new ChangeBufferFactory());
		factoryRegister.register("report-entity", new EntityReporterFactory());
		factoryRegister.register("re", new EntityReporterFactory());
		factoryRegister.register("report-integrity", new IntegrityReporterFactory());
		factoryRegister.register("ri", new IntegrityReporterFactory());
		factoryRegister.register("log-progress", new EntityProgressLoggerFactory());
		factoryRegister.register("lp", new EntityProgressLoggerFactory());
		factoryRegister.register("log-progress-change", new ChangeProgressLoggerFactory());
		factoryRegister.register("lpc", new ChangeProgressLoggerFactory());
		factoryRegister.register("tee", new EntityTeeFactory());
		factoryRegister.register("t", new EntityTeeFactory());
		factoryRegister.register("tee-change", new ChangeTeeFactory());
		factoryRegister.register("tc", new ChangeTeeFactory());
		
		factoryRegister.register("sort-0.6", entitySorterFactory06);
		factoryRegister.register("sort-change-0.6", changeSorterFactory06);
		factoryRegister.register("write-null-0.6", new NullWriterFactory());
		factoryRegister.register("write-null-change-0.6", new NullChangeWriterFactory());
		factoryRegister.register("buffer-0.6", new EntityBufferFactory());
		factoryRegister.register("buffer-change-0.6", new ChangeBufferFactory());
		factoryRegister.register("report-entity-0.6", new EntityReporterFactory());
		factoryRegister.register("report-integrity-0.6", new IntegrityReporterFactory());
		factoryRegister.register("log-progress-0.6", new EntityProgressLoggerFactory());
		factoryRegister.register("log-change-progress-0.6", new ChangeProgressLoggerFactory());
		factoryRegister.register("tee-0.6", new EntityTeeFactory());
		factoryRegister.register("tee-change-0.6", new ChangeTeeFactory());
		factoryRegister.register("tag-sort-0.6", new TagSorterFactory());
		factoryRegister.register("tag-sort-change-0.6", new ChangeTagSorterFactory());
		
		// Register the built-in plugins.
		loadBuiltInPlugins();

		// Register the plugins specified on the command line.
		for (String plugin : plugins) {
			loadPlugin(plugin);
		}
		
		// Register the plugins loaded via JPF.
		loadJPFPlugins();
	}


	private void loadBuiltInPlugins() {
		final String pluginResourceName = "osmosis-plugins.conf";
		
		InputStream pluginInputStream;
		BufferedReader pluginReader;
		
		pluginInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(pluginResourceName);
		if (pluginInputStream == null) {
			throw new OsmosisRuntimeException("Cannot find plugin configuration resource " + pluginResourceName + ".");
		}
		
		try {
			pluginReader = new BufferedReader(new InputStreamReader(pluginInputStream));
			
			for (;;) {
				String plugin;
				
				plugin = pluginReader.readLine();
				if (plugin == null) {
					break;
				}
				
				loadPlugin(plugin);
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to load the plugin configuration from resource "
					+ pluginResourceName + ".", e);
		} finally {
			try {
				pluginInputStream.close();
			} catch (IOException e) {
				LOG.warning("Unable to close plugin resource " + pluginResourceName + ".");
			}
		}
	}


	/**
	 * Loads the tasks implemented as plugins.
	 * 
	 */
	private void loadJPFPlugins() {
		PluginManager pluginManager;
		
		// Create a new JPF plugin manager.
		pluginManager = ObjectFactory.newInstance().createManager();
		
		// Search known locations for plugin files.
		LOG.fine("Searching for JPF plugins.");
		List<PluginLocation> locations = gatherJpfPlugins();
		
		// Register the core plugin.
		LOG.fine("Registering the core plugin.");
		registerCorePlugin(pluginManager);
		
		// Register all located plugins.
		LOG.fine("Registering the extension plugins.");
		if (locations.size() == 0) {
			// There are no plugins available so stop processing here.
		   return;
		}
		registerJpfPlugins(pluginManager, locations);
		
		// Initialise all of the plugins that have been registered.
		LOG.fine("Activating the plugins.");
		// load plugins for the task-extension-point
		PluginDescriptor core = pluginManager.getRegistry()
				.getPluginDescriptor("org.openstreetmap.osmosis.core.plugin.Core");

		ExtensionPoint point = pluginManager.getRegistry().getExtensionPoint(core.getId(), "Task");
		for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {

			Extension ext = it.next();
			PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
			try {
				pluginManager.enablePlugin(descr, true);
				pluginManager.activatePlugin(descr.getId());
				ClassLoader classLoader = pluginManager.getPluginClassLoader(descr);
				loadPluginClass(ext.getParameter("class").valueAsString(), classLoader);
			} catch (PluginLifecycleException e) {
				throw new OsmosisRuntimeException("Cannot load JPF-plugin '" + ext.getId()
						+ "' for extensionpoint '" + ext.getExtendedPointId() + "'", e);
			}
		}
	}


	/**
	 * Register the core plugin from which other plugins will extend.
	 * 
	 * @param pluginManager
	 *            The plugin manager to register the plugin with.
	 */
	private void registerCorePlugin(PluginManager pluginManager) {
		try {
			URL core;
			PluginDescriptor coreDescriptor;
			
			// Get the plugin configuration file.
			core = getClass().getResource("/org/openstreetmap/osmosis/core/plugin/plugin.xml");
			LOG.finest("Plugin URL: " + core);
			
			// Register the core plugin in the plugin registry.
			pluginManager.getRegistry().register(new URL[] {core});
			
			// Get the plugin descriptor from the registry.
			coreDescriptor = pluginManager.getRegistry().getPluginDescriptor(
					"org.openstreetmap.osmosis.core.plugin.Core");
			
			// Enable the plugin.
			pluginManager.enablePlugin(coreDescriptor, true);
			pluginManager.activatePlugin("org.openstreetmap.osmosis.core.plugin.Core");
			
		} catch (ManifestProcessingException e) {
			throw new OsmosisRuntimeException("Unable to register core plugin.", e);
		} catch (PluginLifecycleException e) {
			throw new OsmosisRuntimeException("Unable to enable core plugin.", e);
		}
	}


	/**
	 * Register the given JPF-plugins with the {@link PluginManager}.
	 * 
	 * @param locations
	 *            the plugins found
	 */
	private void registerJpfPlugins(PluginManager pluginManager, List<PluginLocation> locations) {
		if (locations == null) {
			throw new IllegalArgumentException("null plugin-list given");
		}

		try {
			pluginManager.publishPlugins(locations.toArray(new PluginLocation[locations.size()]));
		} catch (JpfException e) {
			throw new OsmosisRuntimeException("Unable to publish plugins.", e);
		}
	}


	/**
	 * @return a list of all JPF-plugins found.
	 */
	private List<PluginLocation> gatherJpfPlugins() {
		File[] pluginsDirs = new File[] {
				new File("plugins"),
				new File(System.getProperty("user.home") + "/.openstreetmap" + File.separator + "osmosis"
						+ File.separator + "plugins"),
				new File(System.getenv("APPDATA") + File.separator + "openstreetmap" + File.separator + "osmosis"
						+ File.separator + "plugins")

		};

		FilenameFilter pluginFileNameFilter = new FilenameFilter() {

			/**
			 * @param dir
			 *            the directory of the file
			 * @param name
			 *            the unqualified name of the file
			 * @return true if this may be a plugin-file
			 */
			public boolean accept(final File dir, final String name) {
				return name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar");
			}
		};
		List<PluginLocation> locations = new LinkedList<PluginLocation>();
		for (File pluginDir : pluginsDirs) {
			LOG.finer("Loading plugins in " + pluginDir.getAbsolutePath());
			if (!pluginDir.exists()) {
				continue;
			}
			File[] plugins = pluginDir.listFiles(pluginFileNameFilter);
			try {
				for (int i = 0; i < plugins.length; i++) {
					LOG.finest("Found plugin " + plugins[i].getAbsolutePath());
					locations.add(StandardPluginLocation.create(plugins[i]));
				}
			} catch (MalformedURLException e) {
				throw new OsmosisRuntimeException("Cannot create plugin location " + pluginDir.getAbsolutePath(), e);
			}
		}
		return locations;
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
			pluginLoader = pluginClass.newInstance();
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
