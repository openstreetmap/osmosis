// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.runner;

import java.util.Collections;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * A task for Osmosis to execute.
 *
 * @author mcuthbert
 */
public class OsmosisTask {
    private final PluginLoader loader;
    private final String name;
    private final Map<String, String> arguments;

    /**
     * Standard Constructor.
     *
     * @param loader The plugin loader containing the task
     * @param name The name of the task
     * @param arguments arguments supplied for the task
     */
    public OsmosisTask(final PluginLoader loader, final String name,
            final Map<String, String> arguments) {
        this.loader = loader;
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Getter for the loader for the task.
     *
     * @return {@link PluginLoader}
     */
    public PluginLoader getLoader() {
        return loader;
    }

    /**
     * Getter for the name, or taskType.
     *
     * @return The name of the TaskType
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the map of arguments for the task.
     *
     * @return Map containing key for argument with the matching value
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * Builds a {@link TaskConfiguration} based on the task.
     *
     * @param taskId A unique id for the task
     * @return {@link TaskConfiguration}
     */
    public TaskConfiguration getTaskConfiguration(final int taskId) {
        return new TaskConfiguration(taskId + "-" + this.name, this.name, Collections.emptyMap(),
                this.arguments, null);
    }
}
