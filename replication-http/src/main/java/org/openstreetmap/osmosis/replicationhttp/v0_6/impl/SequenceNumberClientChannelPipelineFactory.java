// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientChannelPipelineFactory extends SequenceClientChannelPipelineFactory {

	private SequenceNumberClientListener sequenceNumberListener;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param sequenceNumberListener
	 *            This will be notified when new sequence numbers are received.
	 */
	public SequenceNumberClientChannelPipelineFactory(SequenceClientControl control,
			SequenceNumberClientListener sequenceNumberListener) {
		super(control);

		this.sequenceNumberListener = sequenceNumberListener;
	}


	@Override
	protected SequenceClientHandler createHandler(SequenceClientControl control) {
		return new SequenceNumberClientHandler(control, sequenceNumberListener);
	}
}
