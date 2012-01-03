// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


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
	protected void handleRequest(ChannelHandlerContext ctx, ChannelFuture future, HttpRequest request) {
		final String sequenceNumberUri = "sequenceNumber";
		final String contentType = "text/plain";

		// Split the request Uri into its path elements.
		String uri = request.getUri();
		if (!uri.startsWith("/")) {
			throw new OsmosisRuntimeException("Uri doesn't start with a / character: " + uri);
		}
		Queue<String> uriElements = new LinkedList<String>(Arrays.asList(uri.split("/")));
		uriElements.remove(); // First element is empty due to leading '/'.

		// First element must be the sequence number base uri.
		if (uriElements.isEmpty() || !sequenceNumberUri.equals(uriElements.remove())) {
			throw new ResourceNotFoundException();
		}

		/*
		 * The next element determines which replication number to start from.
		 * The request is one of "current" or N where is the last sequence
		 * number received by the client.
		 */
		long lastSequenceNumber;
		if (uriElements.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		String sequenceStartString = uriElements.remove();
		if ("current".equals(sequenceStartString)) {
			// If we want the current number, we tell the controller to start
			// from after the previous number.
			lastSequenceNumber = getControl().getLatestSequenceNumber() - 1;
		} else {
			try {
				lastSequenceNumber = Long.parseLong(sequenceStartString);
			} catch (NumberFormatException e) {
				throw new BadRequestException("Requested sequence number of " + sequenceStartString
						+ " is not a number.");
			}
		}

		// If the next element exists and is "tail" it means that the client
		// wants to stay connected and receive updated sequences as they become
		// available.
		boolean follow;
		if (!uriElements.isEmpty()) {
			String tailElement = uriElements.remove();
			if ("tail".equals(tailElement)) {
				follow = true;
			} else {
				throw new ResourceNotFoundException();
			}
		} else {
			follow = false;
		}
		
		// Validate that that no more URI elements are available.
		if (!uriElements.isEmpty()) {
			throw new ResourceNotFoundException();
		}

		// Begin sending replication sequence information to the client.
		initiateSequenceWriting(ctx, future, contentType, lastSequenceNumber, follow);
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
