package com.bretth.osmosis.core.xml;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.RunnableSourceManager;
import com.bretth.osmosis.core.pipeline.TaskManager;
import com.bretth.osmosis.core.pipeline.TaskManagerFactory;


/**
 * The task manager factory for an xml reader
 * that downloads the map instead of reading it from
 * a file..
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class XmlDownloaderFactory extends TaskManagerFactory {
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final String ARG_URL = "url";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;
	private static final String DEFAULT_URL = "http://www.openstreetmap.org/api/0.4";
	
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskManager createTaskManagerImpl(final String taskId,
                                                final Map<String, String> taskArgs,
                                                final Map<String, String> pipeArgs) {
		double left;
		double right;
		double top;
		double bottom;
		String url;
		
		// Get the task arguments.
		left = getDoubleArgument(taskId, taskArgs, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskId, taskArgs, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskId, taskArgs, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskId, taskArgs, ARG_BOTTOM, DEFAULT_BOTTOM);
		url = getStringArgument(taskId, taskArgs, ARG_URL, DEFAULT_URL);
        
		// Create and return the task and associated manager.
		return new RunnableSourceManager(
			taskId,
			new XmlDownloader(left, right, top, bottom, url),
			pipeArgs
		);
    }
}
