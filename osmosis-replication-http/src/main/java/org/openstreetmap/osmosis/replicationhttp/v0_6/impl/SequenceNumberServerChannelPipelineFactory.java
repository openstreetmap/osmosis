// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

/**
 * Builds Netty channel pipelines for new client connections.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberServerChannelPipelineFactory extends SequenceServerChannelPipelineFactory {

	@Override
	protected SequenceServerHandler createHandler(SequenceServerControl control) {
		return new SequenceNumberServerHandler(control);
	}
}
