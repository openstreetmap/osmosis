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
	protected void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
		final String sequenceNumberUri = "sequenceNumber";
		final String contentType = "text/plain";

		// Split the request Uri into its path elements.
		String uri = request.getUri();
		if (!uri.startsWith("/")) {
			throw new OsmosisRuntimeException("Uri doesn't start with a / character: " + uri);
		}
		Queue<String> uriElements = new LinkedList<String>(Arrays.asList(uri.split("/")));
		
		// First element is empty due to leading '/', unless there is only a '/'
		// in which case there will be no elements.
		if (uriElements.size() > 0) {
			uriElements.remove();
		}

		// First element must be the sequence number base uri.
		if (uriElements.isEmpty() || !sequenceNumberUri.equals(uriElements.remove())) {
			throw new ResourceNotFoundException();
		}

		/*
		 * The next element determines which replication number to start from.
		 * The request is one of "current" or N where is the last sequence
		 * number received by the client.
		 */
		long nextSequenceNumber;
		if (uriElements.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		String sequenceStartString = uriElements.remove();
		if ("current".equals(sequenceStartString)) {
			nextSequenceNumber = getControl().getLatestSequenceNumber();
		} else {
			try {
				nextSequenceNumber = Long.parseLong(sequenceStartString);
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
		initiateSequenceWriting(ctx, contentType, nextSequenceNumber, follow);
	}


	@Override
	protected void writeSequence(ChannelHandlerContext ctx, ChannelFuture future, long sequenceNumber) {
		// Convert the sequence to a string and then a buffer.
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(Long.toString(sequenceNumber), CharsetUtil.UTF_8);

		// Wrap the buffer in a HTTP chunk.
		DefaultHttpChunk chunk = new DefaultHttpChunk(buffer);

		// Pass the chunk downstream.
		Channels.write(ctx, future, chunk);
	}
}
