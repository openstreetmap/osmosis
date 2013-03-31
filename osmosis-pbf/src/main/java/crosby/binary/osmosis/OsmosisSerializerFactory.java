// This software is released into the Public Domain.  See copying.txt for details.
package crosby.binary.osmosis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;

import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

/**
 * The task manager factory for a binary (PBF) writer.
 */
public class OsmosisSerializerFactory extends TaskManagerFactory {
    private static final String ARG_FILE_NAME = "file";
    private static final String DEFAULT_FILE_NAME = "dump.osm.pbf";

    @Override
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        // TODO Auto-generated method stub
        String fileName;
        File file;
        OsmosisSerializer task = null;

        // Get the task arguments.
        fileName = getStringArgument(taskConfig, ARG_FILE_NAME,
                getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME));

        // Create a file object from the file name provided.
        file = new File(fileName);

        // Build the task object.
        try {
            BlockOutputStream output = new BlockOutputStream(
                    new FileOutputStream(file));
            task = new OsmosisSerializer(output);
            task.configBatchLimit(this.getIntegerArgument(taskConfig,
                    "batchlimit", 8000));
            task.configOmit(this.getBooleanArgument(taskConfig, "omitmetadata",
                    false));
            task.setUseDense(this.getBooleanArgument(taskConfig, "usedense",
                true));
            task.configGranularity(this.getIntegerArgument(taskConfig,
                    "granularity", 100));

            output.setCompress(this.getStringArgument(taskConfig, "compress",
                    "deflate"));

        } catch (FileNotFoundException e) {
        	throw new OsmosisRuntimeException("Failed to initialize Osmosis pbf serializer.", e);
        }

        return new SinkManager(taskConfig.getId(), task, taskConfig
                .getPipeArgs());
    }
}
