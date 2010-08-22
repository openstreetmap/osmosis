package crosby.binary.osmosis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;

import crosby.binary.file.BlockOutputStream;

public class OsmosisSerializerFactory extends TaskManagerFactory {
    private static final String ARG_FILE_NAME = "file";
    private static final String DEFAULT_FILE_NAME = "dump.osmbin";

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
            task.configGranularity(this.getIntegerArgument(taskConfig,
                    "granularity", 100));

            output.setCompress(this.getStringArgument(taskConfig, "compress",
                    "deflate"));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new SinkManager(taskConfig.getId(), task, taskConfig
                .getPipeArgs());
    }
}
