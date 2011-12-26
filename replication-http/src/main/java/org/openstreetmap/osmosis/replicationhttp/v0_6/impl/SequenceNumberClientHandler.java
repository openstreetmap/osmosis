// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;


/**
 * Netty handler for receiving replication sequence numbers and notifying
 * listeners.
 * 
 * @author Brett Henderson
 */
public class SequenceNumberClientHandler extends SimpleChannelHandler {

	private static final Logger LOG = Logger.getLogger(SequenceNumberClientHandler.class.getName());

	private SequenceNumberClientControl control;
	private boolean midStream;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceNumberClientHandler(SequenceNumberClientControl control) {
		this.control = control;
	}


	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Send a request to the server asking for sequence number
		// notifications.
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/sequenceNumber/follow");
		Channels.write(ctx, e.getFuture(), request);

		midStream = false;
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer buffer;

		if (!midStream) {
			HttpResponse response = (HttpResponse) e.getMessage();
			buffer = response.getContent();
			midStream = true;
		} else {
			HttpChunk chunk = (HttpChunk) e.getMessage();
			buffer = chunk.getContent();
		}

		if (buffer.readableBytes() > 0) {
			// The readable data is the sequence number in string form.
			String sequenceNumberString = buffer.toString(CharsetUtil.UTF_8);
			long sequenceNumber = Long.parseLong(sequenceNumberString);

			// Send the new sequence number notification.
			control.notifySequenceNumber(sequenceNumber);
		}
	}


	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		control.channelClosed();
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// Get the cause of the exception.
		Throwable t = e.getCause();

		// A ClosedChannelException occurs if the client disconnects and is not
		// an error scenario.
		if (!(t instanceof ClosedChannelException)) {
			LOG.log(Level.SEVERE, "Error during processing.", t);
		}

		// We must stop sending to this client if any errors occur during
		// processing.
		e.getChannel().close();
	}
}
