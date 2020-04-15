// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableSourceManager;
import com.google.common.base.Strings;


/**
 * The task manager factory for a PBF reader.
 * 
 * @author Brett Henderson
 */
public class PbfReaderFactory extends TaskManagerFactory {
	private static final Logger LOG = Logger.getLogger(PbfReaderFactory.class.getName());

 	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm.pbf";
    private static final String ARG_WORKERS = "workers";
    private static final int DEFAULT_WORKERS = 0;
    private static final String ARG_PROXY_HTTP = "proxy";
    private static final String ARG_HTTP_TIMEOUT = "httpTimeout";
    private static final String ARG_HTTP_READ_TIMEOUT = "httpReadTimeout";
    private static final int DEFAULT_TIMEOUT = 60_000;
    private static final int DEFAULT_READ_TIMEOUT = 60_000;
    private static final String FILE_POSTFIX = ".temp.pbf";


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
        PbfReader task;
        int workers;

        // Get the task arguments.
        fileName = getStringArgument(taskConfig, ARG_FILE_NAME,
                getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME));
        workers = getIntegerArgument(taskConfig, ARG_WORKERS, DEFAULT_WORKERS);

        final File file;
        if (fileName.startsWith("http")) {
            final String[] proxy = getStringArgument(taskConfig, ARG_PROXY_HTTP, "").split(":");
            final String proxyHttp = proxy[0];
            final int proxyPort;
            if (proxy.length > 1) {
                proxyPort = Integer.valueOf(proxy[1]);
            } else {
                proxyPort = -1;
            }
            final int timeout = getIntegerArgument(taskConfig, ARG_HTTP_TIMEOUT, DEFAULT_TIMEOUT);
            final int readTimeout = getIntegerArgument(taskConfig, ARG_HTTP_READ_TIMEOUT,
                    DEFAULT_READ_TIMEOUT);

            // Create a file object from the file name provided.
            // if the file starts with http then we should download it first to a temporary directory
            HttpURLConnection connection = null;
            try {
                final String fileLocation =
                        FileUtils.getTempDirectoryPath() + File.separator + System
                                .currentTimeMillis() + FILE_POSTFIX;
                file = new File(fileLocation);
                final URL remoteFile = new URL(fileName);
                connection = this
                        .getURLConnection(remoteFile, proxyHttp, proxyPort, timeout, readTimeout);
                final int remoteFileLength = this
                        .getRemoteFileSize(remoteFile, proxyHttp, proxyPort, timeout, readTimeout);
                LOG.info(String.format("Downloading file %s...", fileName));
                FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
                if (remoteFileLength != file.length()) {
                    throw new RuntimeException(String.format(
                            "Remote file [%s] size [%d] is not equals to local file [%s] size [%d]",
                            fileName, remoteFileLength, fileLocation, file.length()));
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else {
            file = new File(fileName);
        }

        // Build the task object.
        task = new PbfReader(file, workers);

        return new RunnableSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
    }

    private int getRemoteFileSize(final URL url, final String proxyString, final int proxyPort,
            final int timeout, final int readTimeout) {
        HttpURLConnection connection = null;
        try {
            connection = this.getURLConnection(url, proxyString, proxyPort, timeout, readTimeout);
            connection.setRequestMethod("HEAD");
            connection.getInputStream();
            return connection.getContentLength();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection getURLConnection(final URL url, final String proxyString,
            final int proxyPort, final int timeout, final int readTimeout) throws IOException {
        Proxy proxy = null;
        if (!Strings.isNullOrEmpty(proxyString)) {
            final InetSocketAddress address = new InetSocketAddress(proxyString, proxyPort);
            proxy = new Proxy(Proxy.Type.HTTP, address);
        }
        final HttpURLConnection connection;
        if (proxy == null) {
            connection = (HttpURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection(proxy);
        }
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(readTimeout);
        return connection;
    }
}