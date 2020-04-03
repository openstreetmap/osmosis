// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.Osmosis;
import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.TaskRegistrar;
import org.openstreetmap.osmosis.core.pipeline.common.Pipeline;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;

/**
 * Class to run a specific type of Osmosis within Java.
 *
 * @author mcuthbert
 */
public class OsmosisRunner implements Runnable {
    private static final Logger LOG = Logger.getLogger(Osmosis.class.getName());
    private final OsmosisTask inTask;
    private final OsmosisTask outTask;

    /**
     * Standard Constructor. 
     * Todo: This can easily be extended to take a list of tasks and add them all to the pipeline
     *
     * @param inTask The in Task
     * @param outTask The out Task
     */
    public OsmosisRunner(final OsmosisTask inTask, final OsmosisTask outTask) {
        this.inTask = inTask;
        this.outTask = outTask;
    }

    @Override
    public void run() {
        final long startTime = System.currentTimeMillis();
        LOG.info("Osmosis Version " + OsmosisConstants.VERSION);
        final TaskRegistrar taskRegistrar = new TaskRegistrar();
        taskRegistrar.loadPlugin(this.inTask.getLoader());
        taskRegistrar.loadPlugin(this.outTask.getLoader());
        final Pipeline pipeline = new Pipeline(taskRegistrar.getFactoryRegister());

        LOG.info("Preparing pipeline.");
        final List<TaskConfiguration> taskConfigurations = new ArrayList<>();
        taskConfigurations.add(this.inTask.getTaskConfiguration(1));
        taskConfigurations.add(this.outTask.getTaskConfiguration(2));

        pipeline.prepare(taskConfigurations);

        LOG.info("Launching pipeline execution.");
        pipeline.execute();

        LOG.info("Pipeline executing, waiting for completion.");
        pipeline.waitForCompletion();

        LOG.info("Pipeline complete.");

        LOG.info("Total execution time: " + (System.currentTimeMillis() - startTime)
                + " milliseconds.");
    }
}
