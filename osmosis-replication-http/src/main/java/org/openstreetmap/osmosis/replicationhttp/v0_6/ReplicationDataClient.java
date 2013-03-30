// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6;

import java.net.InetSocketAddress;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.ReplicationDataClientChannelPipelineFactory;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClient;
import org.openstreetmap.osmosis.replicationhttp.v0_6.impl.SequenceClientRestartManager;


/**
 * This task connects to a replication data server to obtain replication data,
 * then sends the replication data to the sink. This requires the change sink to
 * support the replication extensions allowing state persistence and multiple
 * invocations.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataClient implements RunnableChangeSource {

	private NoReleaseChangeSinkWrapper changeSinkWrapper;
	private InetSocketAddress serverAddress;
	private String pathPrefix;


	/**
	 * Creates a new instance.
	 * 
	 * @param serverAddress
	 *            The server to connect to.
	 * @param pathPrefix
	 *            The base path to add to the URL. This is necessary if a data
	 *            server is sitting behind a proxy server that adds a prefix to
	 *            the request path.
	 */
	public ReplicationDataClient(InetSocketAddress serverAddress, String pathPrefix) {
		this.serverAddress = serverAddress;
		this.pathPrefix = pathPrefix;

		changeSinkWrapper = new NoReleaseChangeSinkWrapper();
	}


	@Override
	public void setChangeSink(ChangeSink changeSink) {
		changeSinkWrapper.setChangeSink(changeSink);
	}


	@Override
	public void run() {
		try {
			// Create a sequence client restart manager so that our sequence
			// client continues processing in the face of temporary connectivity
			// issues.
			SequenceClientRestartManager clientRestartManager = new SequenceClientRestartManager();
			
			// Create the client for receiving replication data.
			ReplicationDataClientChannelPipelineFactory pipelineFactory =
					new ReplicationDataClientChannelPipelineFactory(
							clientRestartManager.getControl(), changeSinkWrapper, serverAddress.getHostName(),
							pathPrefix);
			SequenceClient client = new SequenceClient(serverAddress, pipelineFactory);

			// Run the client and perform restarts if it fails. This call will
			// block.
			clientRestartManager.manageClient(client);

		} finally {
			changeSinkWrapper.realRelease();
		}
	}

	/**
	 * This acts as a proxy between the sequence client and the real change
	 * sink. The primary purpose is to prevent the release method from being
	 * called until all processing has completed.
	 */
	private static class NoReleaseChangeSinkWrapper implements ChangeSinkChangeSource {
		private ChangeSink changeSink;


		@Override
		public void setChangeSink(ChangeSink changeSink) {
			this.changeSink = changeSink;
		}


		@Override
		public void initialize(Map<String, Object> metaData) {
			changeSink.initialize(metaData);
		}


		@Override
		public void process(ChangeContainer change) {
			changeSink.process(change);
		}


		@Override
		public void complete() {
			changeSink.complete();
		}


		@Override
		public void release() {
			// Do nothing.
		}


		/**
		 * Called by the main replication data client when all processing is
		 * complete. Unlike the release method which does nothing, this calls
		 * the change sink release method.
		 */
		public void realRelease() {
			changeSink.release();
		}
	}
}
