// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.replication.common.ReplicationSequenceFormatter;


/**
 * A sequence server handler implementation that sends the replication data
 * associated with sequence numbers.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataServerHandler extends SequenceServerHandler {

	private static final Logger LOG = Logger.getLogger(ReplicationDataServerHandler.class.getName());
	private static final int CHUNK_SIZE = 8192;

	private File dataDirectory;
	private ReplicationSequenceFormatter sequenceFormatter;
	private FileChannel chunkedFileChannel;
	private boolean chunkedFileCountSent;


	/**
	 * Creates a new instance.
	 * 
	 * @param control
	 *            Provides the Netty handlers with access to the controller.
	 * @param dataDirectory
	 *            The directory containing the replication data files.
	 */
	public ReplicationDataServerHandler(SequenceServerControl control, File dataDirectory) {
		super(control);

		this.dataDirectory = dataDirectory;

		sequenceFormatter = new ReplicationSequenceFormatter(9, 3);
	}


	private FileChannel openFileChannel(File file) {
		try {
			return new FileInputStream(file).getChannel();
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to open file " + file, e);
		}
	}


	private ChannelBuffer readFromFile(FileChannel fileChannel, int bytesToRead) {
		try {
			// Allocate a buffer for the data to be read.
			byte[] rawBuffer = new byte[bytesToRead];

			// Copy data into the buffer using NIO.
			ByteBuffer nioBuffer = ByteBuffer.wrap(rawBuffer);
			for (int bytesRead = 0; bytesRead < bytesToRead;) {
				int lastBytesRead = fileChannel.read(nioBuffer);

				// We always expect to read data.
				if (lastBytesRead < 0) {
					throw new OsmosisRuntimeException("Unexpectedly reached the end of the replication data file");
				}
				if (lastBytesRead == 0) {
					throw new OsmosisRuntimeException("Last read of the replication data file returned 0 bytes");
				}

				bytesRead += lastBytesRead;
			}

			// Create and return a Netty buffer.
			return ChannelBuffers.wrappedBuffer(rawBuffer);

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from the replication data file", e);
		}
	}


	private ChannelBuffer loadFile(File file, ChannelHandlerContext ctx, ChannelFuture future) {
		FileChannel fileChannel = openFileChannel(file);

		try {
			if (fileChannel.size() > Integer.MAX_VALUE) {
				throw new OsmosisRuntimeException("Maximum file size supported is " + Integer.MAX_VALUE + " bytes");
			}

			// Determine the size of the file.
			int fileSize = (int) fileChannel.size();

			// Read the entire file.
			ChannelBuffer buffer = readFromFile(fileChannel, fileSize);

			// We no longer need access to the file.
			fileChannel.close();

			return buffer;

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from file " + file, e);
		} finally {
			try {
				fileChannel.close();
			} catch (IOException e) {
				LOG.log(Level.WARNING, "Unable to close channel for file " + file, e);
			}
		}
	}


	private ChannelBuffer getFileChunk() {
		try {
			// Determine how many bytes are left in the file.
			long remaining = chunkedFileChannel.size() - chunkedFileChannel.position();

			// We will only send up to our maximum chunk size.
			if (remaining > CHUNK_SIZE) {
				remaining = CHUNK_SIZE;
			}

			// Read the next data for the next chunk.
			ChannelBuffer buffer = readFromFile(chunkedFileChannel, (int) remaining);

			// Close the file if we've reached the end.
			if (chunkedFileChannel.position() >= chunkedFileChannel.size()) {
				chunkedFileChannel.close();
				chunkedFileChannel = null;
			}

			return buffer;

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read from the replication data file", e);
		}
	}


	@Override
	protected void handleRequest(ChannelHandlerContext ctx, ChannelFuture future, HttpRequest request) {
		final String sequenceNumberUri = "replicationData";
		final String contentType = "application/octet-stream";
		
		// Split the request Uri into its path elements.
		String uri = request.getUri();
		if (!uri.startsWith("/")) {
			throw new OsmosisRuntimeException("Uri doesn't start with a / character: " + uri);
		}
		String[] uriElements = uri.split("/");

		if (uriElements.length == 2 && uriElements[1].equals(sequenceNumberUri)) {
			initiateSequenceWriting(ctx, future, contentType, getControl().getLatestSequenceNumber() - 1, false);
		} else if (uriElements.length == 3 && uriElements[1].equals(sequenceNumberUri)
				&& uriElements[2].equals("follow")) {
			initiateSequenceWriting(ctx, future, contentType, getControl().getLatestSequenceNumber() - 1, true);
		} else {
			write404(ctx, future, uri);
		}
	}


	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		// We do not support sending new replication data until the previous
		// send has completed.
		if (chunkedFileChannel != null) {
			throw new OsmosisRuntimeException(
					"We cannot send new replication data until the previous write has completed");
		}

		// The message event is a Long containing the sequence number.
		long sequenceNumber = (Long) e.getMessage();

		// Get the name of the replication data file.
		String stateFileName = sequenceFormatter.getFormattedName(sequenceNumber, ".state.txt");
		String dataFileName = sequenceFormatter.getFormattedName(sequenceNumber, ".osc.gz");
		File stateFile = new File(dataDirectory, stateFileName);
		File dataFile = new File(dataDirectory, dataFileName);

		// Load the contents of the state file.
		ChannelBuffer stateFileBuffer = loadFile(stateFile, ctx, e.getFuture());

		// Open the data file read for sending.
		chunkedFileChannel = openFileChannel(dataFile);
		chunkedFileCountSent = false;

		/*
		 * Send the state file to the client. We will continue when we receive
		 * completion information via the writeComplete method. We must create a
		 * new future because we don't want the future of the current event to
		 * fire until we're completely finished processing.
		 */
		Channels.write(ctx, Channels.future(ctx.getChannel()), new DefaultHttpChunk(stateFileBuffer));
	}


	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		if (chunkedFileChannel != null) {
			// We have an open file channel so we are still sending replication
			// data.
			if (!chunkedFileCountSent) {
				// Calculate the number of chunks to be sent and send to the
				// client.
				long fileSize = chunkedFileChannel.size();
				long numChunks = fileSize / CHUNK_SIZE;
				if ((fileSize % CHUNK_SIZE) > 0) {
					numChunks++;
				}

				// Send the number of chunks as a string.
				ChannelBuffer numChunksBuffer = ChannelBuffers
						.copiedBuffer(Long.toString(numChunks), CharsetUtil.UTF_8);
				chunkedFileCountSent = true;
				Channels.write(ctx, Channels.future(ctx.getChannel()), new DefaultHttpChunk(numChunksBuffer));

			} else {
				// Send the next chunk to the client.
				Channels.write(ctx, Channels.future(ctx.getChannel()), new DefaultHttpChunk(getFileChunk()));
			}
		}
	}


	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Close the in-progress chunk file channel if it exists.
		if (chunkedFileChannel != null) {
			try {
				chunkedFileChannel.close();
			} catch (IOException ex) {
				LOG.log(Level.WARNING, "Unable to close the replication data file.", ex);
			}
			chunkedFileChannel = null;
		}
	}
}
