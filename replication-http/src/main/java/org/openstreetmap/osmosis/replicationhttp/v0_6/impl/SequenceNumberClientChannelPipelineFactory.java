// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;



/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientChannelPipelineFactory extends
		SequenceClientChannelPipelineFactory<SequenceNumberClientControl> {

	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceNumberClientChannelPipelineFactory(SequenceNumberClientControl control) {
		super(control);
	}


	@Override
	protected SequenceClientHandler createHandler(SequenceNumberClientControl control) {
		return new SequenceNumberClientHandler(control);
	}
}
