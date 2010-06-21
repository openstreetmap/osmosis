// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.ChangeSinkManager;
import org.openstreetmap.osmosis.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an {@link XmlChangeUploader}.
 *
 * @author Marcus Wolschon MArcus@Wolschon.biz
 */
public class XmlChangeUploaderFactory extends XmlTaskManagerFactory {

    /**
     * Argument-name for the username.
     */
    private static final String ARG_USER_NAME = "user";
    /**
     * Argument-name for the password.
     */
    private static final String ARG_PASSWORD = "password";
    /**
     * Argument-name for the baseurl.
     */
    private static final String ARG_BASEURL = "server";
    /**
     * Argument-name for the comment for the changeset.
     */
    private static final String ARG_COMMENT = "comment";

    /**
     * {@inheritDoc}
     */
    @Override
    protected final TaskManager createTaskManagerImpl(
            final TaskConfiguration taskConfig) {

        // Get the task arguments.
        String userName = getStringArgument(
            taskConfig,
            ARG_USER_NAME
        );
        String password = getStringArgument(
                taskConfig,
                ARG_PASSWORD
            );
        String baseURL = getStringArgument(
            taskConfig,
            ARG_BASEURL,
            null
        );
        String comment = getStringArgument(
                taskConfig,
                ARG_COMMENT,
                ""
            );

        // Build the task object.
       XmlChangeUploader task = new XmlChangeUploader(
               baseURL,
               userName,
               password,
               comment);

        return new ChangeSinkManager(taskConfig.getId(),
                                     task, taskConfig.getPipeArgs());
    }
}
