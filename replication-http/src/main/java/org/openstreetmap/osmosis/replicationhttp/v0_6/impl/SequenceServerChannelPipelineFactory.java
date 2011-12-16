// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpServerCodec;


/**
 * Builds Netty channel pipelines for new client connections.
 * 
 * @author Brett Henderson
 */
public class SequenceServerChannelPipelineFactory implements ChannelPipelineFactory {

	private SequenceServerControl control;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceServerChannelPipelineFactory(SequenceServerControl control) {
		this.control = control;
	}


	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return Channels.pipeline(new HttpServerCodec(), new SequenceServerHandler(control));
	}
}
