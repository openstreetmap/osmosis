// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Netty handler for sending replication sequence numbers to clients.
 * 
 * @author Brett Henderson
 */
public abstract class SequenceServerHandler extends SimpleChannelHandler {

	private static final Logger LOG = Logger.getLogger(SequenceServerHandler.class.getName());

	private SequenceServerControl control;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceServerHandler(SequenceServerControl control) {
		this.control = control;
	}


	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		control.registerChannel(e.getChannel());
	}


	private void write404(ChannelHandlerContext ctx, final MessageEvent e, String requestedUri) {
		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
		response.addHeader("Content-Type", "text/plain");

		// Send the 404 message for the client.
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer("The requested URI doesn't exist: " + requestedUri,
				CharsetUtil.UTF_8);
		response.setContent(buffer);
		Channels.write(ctx, e.getFuture(), response);

		// Wait for the previous operation to finish and then close the channel.
		e.getFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				e.getChannel().close();
			}
		});
	}


	private void writeSequence(ChannelHandlerContext ctx, final MessageEvent e, final boolean follow) {
		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.addHeader("Content-Type", "text/plain");
		response.setChunked(true);
		response.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
		Channels.write(ctx, e.getFuture(), response);

		// Wait for the previous operation to finish and then start sending
		// sequence numbers to this channel.
		e.getFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					control.sendSequence(e.getChannel(), follow);
				}
			}
		});
	}


	/**
	 * Gets the URI that all requests must provide. Note that this can only
	 * consist of a single path element.
	 * 
	 * @return The URI.
	 */
	protected abstract String getUri();


	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		final String sequenceNumberUri = "sequenceNumber";

		// We have received a message from the client which is a HTTP request.
		HttpRequest request = (HttpRequest) e.getMessage();

		// Split the request Uri into its path elements.
		String uri = request.getUri();
		if (!uri.startsWith("/")) {
			throw new OsmosisRuntimeException("Uri doesn't start with a / character: " + uri);
		}
		String[] uriElements = uri.split("/");

		if (uriElements.length == 2 && uriElements[1].equals(sequenceNumberUri)) {
			writeSequence(ctx, e, false);
		} else if (uriElements.length == 3 && uriElements[1].equals(sequenceNumberUri)
				&& uriElements[2].equals("follow")) {
			writeSequence(ctx, e, true);
		} else {
			write404(ctx, e, uri);
		}
	}


	@Override
	public abstract void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception;


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
