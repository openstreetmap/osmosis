// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6;

 import java.io.File;

 import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;

 /**
 * Base Factory for the replication classes, specifically for the working directory argument.
 *
 * @author mcuthbert
 */
public abstract class WorkingTaskManagerFactory extends TaskManagerFactory {
    private static final String ARG_WORKING_DIRECTORY = "workingDirectory";
    private static final String DEFAULT_WORKING_DIRECTORY = "./";

     /**
     * Gets the current working directory for the task.
     *
     * @param taskConfig {@link TaskConfiguration}
     * @return {@link File}
     */
    protected File getWorkingDirectory(final TaskConfiguration taskConfig) {
        return new File(this.getStringArgument(
                taskConfig,
                ARG_WORKING_DIRECTORY,
                getDefaultStringArgument(taskConfig, DEFAULT_WORKING_DIRECTORY)
        ));
    }
}
