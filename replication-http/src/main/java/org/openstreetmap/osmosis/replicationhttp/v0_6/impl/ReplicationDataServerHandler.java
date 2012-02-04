// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.util.PropertiesPersister;
import org.openstreetmap.osmosis.replication.common.ReplicationSequenceFormatter;
import org.openstreetmap.osmosis.replication.common.ReplicationState;


/**
 * A sequence server handler implementation that sends the replication data
 * associated with sequence numbers.
 * 
 * @author Brett Henderson
 */
public class ReplicationDataServerHandler extends SequenceServerHandler {

	private static final Logger LOG = Logger.getLogger(ReplicationDataServerHandler.class.getName());
	private static final String REQUEST_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";
	private static final int CHUNK_SIZE = 4096;

	private File dataDirectory;
	private ReplicationSequenceFormatter sequenceFormatter;
	private FileChannel chunkedFileChannel;
	private boolean fileSizeSent;
	private boolean includeData;
	private ChannelFuture sequenceFuture;


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


	private DateFormat getRequestDateParser() {
		SimpleDateFormat dateParser = new SimpleDateFormat(REQUEST_DATE_FORMAT);
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		dateParser.setCalendar(calendar);

		return dateParser;
	}


	private File getStateFile(long sequenceNumber) {
		return new File(dataDirectory, sequenceFormatter.getFormattedName(sequenceNumber, ".state.txt"));
	}


	private File getDataFile(long sequenceNumber) {
		return new File(dataDirectory, sequenceFormatter.getFormattedName(sequenceNumber, ".osc.gz"));
	}


	private ReplicationState getReplicationState(long sequenceNumber) {
		PropertiesPersister persister = new PropertiesPersister(getStateFile(sequenceNumber));
		ReplicationState state = new ReplicationState();
		state.load(persister.loadMap());

		return state;
	}


	/**
	 * Search through the replication state records and find the nearest
	 * replication number with a timestamp earlier or equal to the requested
	 * date. It is not sufficient to find the minimum known sequence record with
	 * a timestamp greater than the requested date because there may be missing
	 * replication records in between.
	 * 
	 * @param lastDate
	 *            The last date known by the client.
	 * @return The associated sequence number.
	 */
	private long getNextSequenceNumberByDate(Date lastDate) {
		long startBound = 0;
		long endBound = getControl().getLatestSequenceNumber();

		// If the requested date is greater than or equal to the latest known
		// timestamp we should return our latest sequence number so that the
		// client will start receiving all new records as they arrive with
		// possibly some duplicated change records.
		if (lastDate.compareTo(getReplicationState(endBound).getTimestamp()) >= 0) {
			return endBound;
		}

		// Continue splitting our range in half until either we find the
		// requested record, or we only have one possibility remaining.
		while ((endBound - startBound) > 1) {
			// Calculate the current midpoint.
			long midPoint = startBound + ((endBound - startBound) / 2);

			// If the midpoint doesn't exist we need to reset the start bound to
			// the midpoint and search again.
			if (!getStateFile(midPoint).exists()) {
				startBound = midPoint;
				continue;
			}

			// If the midpoint timestamp is greater we search in the lower half,
			// otherwise the higher half.
			int comparison = lastDate.compareTo(getReplicationState(midPoint).getTimestamp());
			if (comparison == 0) {
				// We have an exact match so stop processing now.
				return midPoint;
			} else if (comparison < 0) {
				// We will now search in the lower half of the search range.
				// Even though we know the midpoint is not the right value, we
				// include it in the next range because our search assumes that
				// the right sequence number is less than the end point.
				endBound = midPoint;
			} else {
				// We will now search in the upper half of the search range.
				// Even though the mid point has a timestamp less than the
				// requested value, it still may be the selected value if the
				// next timestamp is greater.
				startBound = midPoint;
			}
		}

		// We only have one possibility remaining which is the start bound. This
		// is the requested record if it exists and has a timestamp less than or
		// equal to that requested.
		if (getStateFile(startBound).exists()
				&& lastDate.compareTo(getReplicationState(startBound).getTimestamp()) >= 0) {
			return startBound;
		} else {
			// We cannot find any replication records with an early enough date.
			// This typically means that replication records for that time
			// period either no longer exist or never existed.
			throw new ResourceGoneException();
		}
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


	private ChannelBuffer loadFile(File file) {
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
	
	
	private ChannelBuffer buildChunkHeader(long chunkSize) {
		return ChannelBuffers.copiedBuffer(Long.toString(chunkSize) + "\r\n", CharsetUtil.UTF_8);
	}


	@Override
	protected void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
		final String replicationStateUri = "replicationState";
		final String replicationDataUri = "replicationData";
		final String textContentType = "text/plain";
		final String dataContentType = "application/octet-stream";

		// Split the request Uri into its path elements.
		String uri = request.getUri();
		if (!uri.startsWith("/")) {
			throw new OsmosisRuntimeException("Uri doesn't start with a / character: " + uri);
		}
		Queue<String> uriElements = new LinkedList<String>(Arrays.asList(uri.split("/")));
		uriElements.remove(); // First element is empty due to leading '/'.

		// First element must be either the replication state or replication
		// data uri which determines whether replication data will be included
		// or just the replication state.
		String contentType;
		if (uriElements.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		String requestTypeString = uriElements.remove();
		if (replicationStateUri.equals(requestTypeString)) {
			contentType = textContentType;
			includeData = false;
		} else if (replicationDataUri.equals(requestTypeString)) {
			contentType = dataContentType;
			includeData = true;
		} else {
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
			// Try to parse the sequence start string as a number. If that fails
			// try to parse as a date.
			try {
				nextSequenceNumber = Long.parseLong(sequenceStartString);
			} catch (NumberFormatException e) {
				try {
					Date lastDate = getRequestDateParser().parse(sequenceStartString);
					nextSequenceNumber = getNextSequenceNumberByDate(lastDate);

				} catch (ParseException e1) {
					throw new BadRequestException("Requested sequence number of " + sequenceStartString
							+ " is not a number, or a date in format yyyy-MM-dd-HH-mm-ss.");
				}

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
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("New request details, includeData=" + includeData + ", sequenceNumber=" + nextSequenceNumber
					+ ", tail=" + follow);
		}
		initiateSequenceWriting(ctx, contentType, nextSequenceNumber, follow);
	}


	@Override
	protected void writeSequence(ChannelHandlerContext ctx, ChannelFuture future, long sequenceNumber) {
		// We do not support sending new replication data until the previous
		// send has completed.
		if (chunkedFileChannel != null) {
			throw new OsmosisRuntimeException(
					"We cannot send new replication data until the previous write has completed");
		}
		
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Sequence being written, includeData=" + includeData + ", sequenceNumber="
					+ sequenceNumber);
		}
		
		// We must save the future to attach to the final write.
		sequenceFuture = future;

		// Get the name of the replication data file.
		File stateFile = getStateFile(sequenceNumber);
		File dataFile = getDataFile(sequenceNumber);

		// Load the contents of the state file.
		ChannelBuffer stateFileBuffer = loadFile(stateFile);
		
		// Add a chunk length header.
		stateFileBuffer = ChannelBuffers.wrappedBuffer(buildChunkHeader(stateFileBuffer.readableBytes()),
				stateFileBuffer);

		// Only include replication data if initially requested by the client
		// and if this is not sequence 0.
		if (includeData && sequenceNumber > 0) {
			// Open the data file read for sending.
			chunkedFileChannel = openFileChannel(dataFile);
			fileSizeSent = false;
		}

		/*
		 * Send the state file to the client. If replication data is to be sent
		 * we will continue when we receive completion information via the
		 * writeComplete method. We must create a new future now if we have more
		 * data coming because we don't want the future of the current event to
		 * fire until we're completely finished processing.
		 */
		ChannelFuture writeFuture;
		if (chunkedFileChannel != null) {
			writeFuture = Channels.future(ctx.getChannel());
		} else {
			writeFuture = sequenceFuture;
		}
		Channels.write(ctx, writeFuture, new DefaultHttpChunk(stateFileBuffer));
	}


	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		if (chunkedFileChannel != null) {
			// We have an open file channel so we are still sending replication
			// data.
			ChannelBuffer buffer;
			ChannelFuture future;
			if (!fileSizeSent) {
				// Send a chunk header containing the size of the file.
				ChannelBuffer fileSizeBuffer = buildChunkHeader(chunkedFileChannel.size());
				fileSizeSent = true;
				future = Channels.future(ctx.getChannel());
				buffer = fileSizeBuffer;
			} else {
				// Send the next chunk to the client.
				buffer = getFileChunk();
				if (chunkedFileChannel != null) {
					future = Channels.future(ctx.getChannel());
				} else {
					// This is the last write for this sequence so attach the original future.
					future = sequenceFuture;
				}
			}
			
			// Write the data to the channel.
			Channels.write(ctx, future, new DefaultHttpChunk(buffer));
		} else {
			super.writeComplete(ctx, e);
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
		super.channelClosed(ctx, e);
	}
}
