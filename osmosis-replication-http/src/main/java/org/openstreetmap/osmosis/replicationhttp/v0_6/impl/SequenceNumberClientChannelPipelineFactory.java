// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientChannelPipelineFactory extends SequenceClientChannelPipelineFactory {

	private SequenceNumberClientListener sequenceNumberListener;
	private String serverHost;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param sequenceNumberListener
	 *            This will be notified when new sequence numbers are received.
	 * @param serverHost
	 *            The name of the host system running the sequence server.
	 */
	public SequenceNumberClientChannelPipelineFactory(SequenceClientControl control,
			SequenceNumberClientListener sequenceNumberListener, String serverHost) {
		super(control);

		this.serverHost = serverHost;
		this.sequenceNumberListener = sequenceNumberListener;
	}


	@Override
	protected SequenceClientHandler createHandler(SequenceClientControl control) {
		return new SequenceNumberClientHandler(control, sequenceNumberListener, serverHost);
	}
}
