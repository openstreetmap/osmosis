// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;


/**
 * Builds Netty channel pipelines for new connections to servers.
 * 
 * 
 * @author Brett Henderson
 */
public abstract class SequenceClientChannelPipelineFactory implements
		ChannelPipelineFactory {

	private SequenceClientControl centralControl;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceClientChannelPipelineFactory(SequenceClientControl control) {
		this.centralControl = control;
	}


	/**
	 * Creates a handler to be used for processing channel messages.
	 * 
	 * @param control
	 *            The control object used to send event notifications.
	 * @return The channel handler.
	 */
	protected abstract SequenceClientHandler createHandler(SequenceClientControl control);


	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return Channels.pipeline(new HttpClientCodec(), createHandler(centralControl));
	}
}
