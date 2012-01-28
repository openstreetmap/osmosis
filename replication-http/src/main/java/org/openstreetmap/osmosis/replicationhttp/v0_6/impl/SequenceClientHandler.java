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


/**
 * Netty handler for receiving replication sequence information and notifying
 * listeners.
 * 
 * @author Brett Henderson
 */
public abstract class SequenceClientHandler extends SimpleChannelHandler {

	private static final Logger LOG = Logger.getLogger(SequenceClientHandler.class.getName());

	private SequenceClientControl control;
	private String serverHost;
	private boolean midStream;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param serverHost
	 *            The name of the host system running the sequence server.
	 */
	public SequenceClientHandler(SequenceClientControl control, String serverHost) {
		this.control = control;
		this.serverHost = serverHost;
	}


	/**
	 * Gets the URI to request from the server to initialise message processing.
	 * 
	 * @return The request URI.
	 */
	protected abstract String getRequestUri();


	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Send a request to the server asking for sequence number
		// notifications.
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, getRequestUri());
		request.addHeader("Host", serverHost);
		Channels.write(ctx, e.getFuture(), request);

		midStream = false;
	}


	/**
	 * Processes the contents of a single HTTP chunk.
	 * 
	 * @param buffer
	 *            The data contained in the chunk.
	 */
	protected abstract void processMessageData(ChannelBuffer buffer);


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

		// Perform implementation specific processing of the buffer contents.
		if (buffer.readableBytes() > 0) {
			processMessageData(buffer);
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
			LOG.log(Level.SEVERE, "Error during processing for channel " + ctx.getChannel() + ".", t);
		}

		// We must stop sending to this client if any errors occur during
		// processing.
		e.getChannel().close();
	}
}
