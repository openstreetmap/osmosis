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
public abstract class SequenceServerChannelPipelineFactory implements ChannelPipelineFactory {

	private SequenceServerControl centralControl;


	/**
	 * Provides handlers with access to server control functions.
	 * 
	 * @param control
	 *            The new control object.
	 */
	public void setControl(SequenceServerControl control) {
		this.centralControl = control;
	}


	/**
	 * Creates a handler to be used for processing channel messages.
	 * 
	 * @param control
	 *            The control object used to send event notifications.
	 * @return The channel handler.
	 */
	protected abstract SequenceServerHandler createHandler(SequenceServerControl control);


	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return Channels.pipeline(new HttpServerCodec(), createHandler(centralControl));
	}
}
