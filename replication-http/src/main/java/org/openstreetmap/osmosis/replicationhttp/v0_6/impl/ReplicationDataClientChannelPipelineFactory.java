// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataClientChannelPipelineFactory extends SequenceClientChannelPipelineFactory {

	private ChangeSink changeSink;
	private String serverHost;
	private String pathPrefix;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param changeSink
	 *            The destination for the replication data.
	 * @param serverHost
	 *            The name of the host system running the sequence server.
	 * @param pathPrefix
	 *            The base path to add to the URL. This is necessary if a data
	 *            server is sitting behind a proxy server that adds a prefix to
	 *            the request path.
	 */
	public ReplicationDataClientChannelPipelineFactory(SequenceClientControl control,
			ChangeSink changeSink, String serverHost, String pathPrefix) {
		super(control);

		this.changeSink = changeSink;
		this.serverHost = serverHost;
		this.pathPrefix = pathPrefix;
	}


	@Override
	protected SequenceClientHandler createHandler(SequenceClientControl control) {
		return new ReplicationDataClientHandler(control, changeSink, serverHost, pathPrefix);
	}
}
