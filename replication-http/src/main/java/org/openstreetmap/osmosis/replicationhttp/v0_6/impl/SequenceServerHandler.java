// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.net.InetSocketAddress;
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


/**
 * Netty handler for sending replication sequence numbers to clients.
 * 
 * @author Brett Henderson
 */
public abstract class SequenceServerHandler extends SimpleChannelHandler {

	private static final Logger LOG = Logger.getLogger(SequenceServerHandler.class.getName());

	private SequenceServerControl control;
	private long currentSequenceNumber;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 */
	public SequenceServerHandler(SequenceServerControl control) {
		this.control = control;
	}


	/**
	 * Gets the central control object.
	 * 
	 * @return The controller.
	 */
	protected SequenceServerControl getControl() {
		return control;
	}


	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		control.registerChannel(e.getChannel());
	}


	/**
	 * Writes a HTTP 404 response to the client.
	 * 
	 * @param ctx
	 *            The Netty context.
	 * @param requestedUri
	 *            The URI requested by the client.
	 */
	private void writeResourceNotFound(final ChannelHandlerContext ctx, String requestedUri) {
		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
		response.addHeader("Content-Type", "text/plain");

		// Send the 404 message to the client.
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer("The requested resource does not exist: " + requestedUri,
				CharsetUtil.UTF_8);
		response.setContent(buffer);

		// Write the header. Use a new future because the future we've been
		// passed is for upstream.
		ChannelFuture headerFuture = Channels.future(ctx.getChannel());
		Channels.write(ctx, headerFuture, response);

		// Wait for the previous operation to finish and then close the channel.
		headerFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				ctx.getChannel().close();
			}
		});
	}


	/**
	 * Writes a HTTP 410 response to the client.
	 * 
	 * @param ctx
	 *            The Netty context.
	 * @param requestedUri
	 *            The URI requested by the client.
	 */
	private void writeResourceGone(final ChannelHandlerContext ctx, String requestedUri) {
		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.GONE);
		response.addHeader("Content-Type", "text/plain");

		// Send the 410 message to the client.
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer("The requested resource is no longer available: "
				+ requestedUri, CharsetUtil.UTF_8);
		response.setContent(buffer);

		// Write the header. Use a new future because the future we've been
		// passed is for upstream.
		ChannelFuture headerFuture = Channels.future(ctx.getChannel());
		Channels.write(ctx, headerFuture, response);

		// Wait for the previous operation to finish and then close the channel.
		headerFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				ctx.getChannel().close();
			}
		});
	}


	/**
	 * Writes a HTTP 400 response to the client.
	 * 
	 * @param ctx
	 *            The Netty context.
	 * @param requestedUri
	 *            The URI requested by the client.
	 * @param errorMessage
	 *            Further information about why the request is bad.
	 */
	private void writeBadRequest(final ChannelHandlerContext ctx, String requestedUri,
			String errorMessage) {
		final String newLine = "\r\n";

		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
		response.addHeader("Content-Type", "text/plain");

		// Send the 400 message to the client.
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("Bad Request").append(newLine);
		messageBuilder.append("Message: ").append(errorMessage).append(newLine);
		messageBuilder.append("Requested URI: ").append(requestedUri).append(newLine);
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(messageBuilder.toString(), CharsetUtil.UTF_8);
		response.setContent(buffer);

		// Write the header. Use a new future because the future we've been
		// passed is for upstream.
		ChannelFuture headerFuture = Channels.future(ctx.getChannel());
		Channels.write(ctx, headerFuture, response);

		// Wait for the previous operation to finish and then close the channel.
		headerFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				ctx.getChannel().close();
			}
		});
	}


	/**
	 * Writes server statistics to the client.
	 * 
	 * @param ctx
	 *            The Netty context.
	 */
	private void writeStatistics(final ChannelHandlerContext ctx) {
		final String newLine = "\r\n";
		
		ServerStatistics statistics = control.getStatistics();

		// Write the HTTP header to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
		response.addHeader("Content-Type", "text/plain");

		// Send the statistics message to the client.
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append("Server Statistics").append(newLine);
		messageBuilder.append("Total Requests: ").append(statistics.getTotalRequests()).append(newLine);
		messageBuilder.append("Active Connections: ").append(statistics.getActiveConnections()).append(newLine);
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(messageBuilder.toString(), CharsetUtil.UTF_8);
		response.setContent(buffer);

		// Write the header. Use a new future because the future we've been
		// passed is for upstream.
		ChannelFuture headerFuture = Channels.future(ctx.getChannel());
		Channels.write(ctx, headerFuture, response);

		// Wait for the previous operation to finish and then close the channel.
		headerFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				ctx.getChannel().close();
			}
		});
	}


	/**
	 * Writes sequence data to the client. If follow is set, it allows
	 * continuous updates to be streamed to the client.
	 * 
	 * @param ctx
	 *            The Netty context.
	 * @param contentType
	 *            The content type to set on the HTTP response.
	 * @param requestedSequenceNumber
	 *            The requested sequence number. Sending will start from this
	 *            number.
	 * @param follow
	 *            If true, continuous updates will be sent to the client.
	 */
	protected void initiateSequenceWriting(final ChannelHandlerContext ctx,
			String contentType, final long requestedSequenceNumber, final boolean follow) {
		// Create the HTTP header to send to the client.
		DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.addHeader("Content-Type", contentType);
		response.setChunked(true);
		response.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

		// Write the header. We must use a new future because the future we've
		// been passed is for upstream.
		ChannelFuture headerFuture = Channels.future(ctx.getChannel());
		Channels.write(ctx, headerFuture, response);

		// Wait for the previous operation to finish and then start sending
		// sequence numbers to this channel.
		headerFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					control.determineNextChannelAction(ctx.getChannel(), requestedSequenceNumber, follow);
				}
			}
		});
	}


	/**
	 * Parses the request and initialises the response processing, typically by
	 * calling the writeSequence method.
	 * 
	 * @param ctx
	 *            The Netty context.
	 * @param request
	 *            The client request.
	 */
	protected abstract void handleRequest(ChannelHandlerContext ctx, HttpRequest request);


	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// We have received a message from the client which is a HTTP request.
		HttpRequest request = (HttpRequest) e.getMessage();
		
		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Received new request from " + remoteAddress.getAddress().getHostAddress() + ":"
					+ remoteAddress.getPort());
		}

		// Process the HTTP request.
		try {
			// Check if this is a request to a generic URL. If it isn't
			// something we support then delegate to the specific handler.
			if (request.getUri().equals("/statistics")) {
				writeStatistics(ctx);
			} else {
				handleRequest(ctx, request);
			}
			
		} catch (ResourceNotFoundException ex) {
			writeResourceNotFound(ctx, request.getUri());
		} catch (ResourceGoneException ex) {
			writeResourceGone(ctx, request.getUri());
		} catch (BadRequestException ex) {
			writeBadRequest(ctx, request.getUri(), ex.getMessage());
		}
	}


	/**
	 * Convert the sequence number to sequence data and write to the channel.
	 * 
	 * @param ctx
	 *            The channel handler context.
	 * @param future
	 *            The future for current processing.
	 * @param sequenceNumber
	 *            The sequence number to be written.
	 */
	protected abstract void writeSequence(ChannelHandlerContext ctx, ChannelFuture future, long sequenceNumber);


	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// The message event is a Long containing the sequence number.
		currentSequenceNumber = (Long) e.getMessage();

		// Call the concrete implementation to convert the sequence to writable
		// data.
		writeSequence(ctx, e.getFuture(), currentSequenceNumber);
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

	/**
	 * Used during request parsing to notify that the requested URI could not be
	 * found.
	 */
	protected static class ResourceNotFoundException extends RuntimeException {
		private static final long serialVersionUID = -1L;
	}

	/**
	 * Used during request parsing to notify that the request is invalid in some
	 * way.
	 */
	protected static class BadRequestException extends RuntimeException {
		private static final long serialVersionUID = -1L;


		/**
		 * Creates a new instance.
		 * 
		 * @param message
		 *            The error message.
		 */
		public BadRequestException(String message) {
			super(message);
		}
	}

	/**
	 * Used during request parsing to notify that the requested URI is no longer
	 * available.
	 */
	protected static class ResourceGoneException extends RuntimeException {
		private static final long serialVersionUID = -1L;
	}
}
