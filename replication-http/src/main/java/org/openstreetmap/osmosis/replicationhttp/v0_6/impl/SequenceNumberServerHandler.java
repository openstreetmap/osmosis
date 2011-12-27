// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.util.CharsetUtil;

/**
 * A sequence server handler implementation that sends the sequence number
 * itself.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberServerHandler extends SequenceServerHandler {

	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceNumberServerHandler(SequenceServerControl control) {
		super(control);
	}


	@Override
	protected String getUri() {
		return "sequenceNumber";
	}


	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// The message event is a Long containing the sequence number.
		long sequenceNumber = (Long) e.getMessage();

		// Convert the sequence to a string and then a buffer.
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(Long.toString(sequenceNumber), CharsetUtil.UTF_8);

		// Wrap the buffer in a HTTP chunk.
		DefaultHttpChunk chunk = new DefaultHttpChunk(buffer);

		// Pass the chunk downstream.
		Channels.write(ctx, e.getFuture(), chunk);
	}
}
