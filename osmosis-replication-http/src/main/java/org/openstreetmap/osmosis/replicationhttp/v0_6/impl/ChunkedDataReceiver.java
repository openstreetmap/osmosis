// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replicationhttp.v0_6.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Provides the ability to read data that has been broken into data chunks. Each
 * chunk is preceded by the number of bytes in the chunk followed by a carriage
 * return line feed pair.
 * 
 * @author Brett Henderson
 */
public class ChunkedDataReceiver implements Releasable {

	private static final Logger LOG = Logger.getLogger(ChunkedDataReceiver.class.getName());

	private ChannelBuffer buffer;
	private File tmpDataFile;
	private FileChannel tmpDataChannel;
	private List<File> readyFiles;
	private boolean chunkInProgress;
	private long bytesRemaining;


	/**
	 * Creates a new instance.
	 */
	public ChunkedDataReceiver() {
		buffer = ChannelBuffers.dynamicBuffer();
		
		readyFiles = new ArrayList<File>();
		chunkInProgress = false;
	}


	/**
	 * Attempts to read a chunk length header from the data currently in the
	 * buffer. If a carriage return line feed pair is found, then the data
	 * preceeding those characters will be converted to a number and returned.
	 * If the carriage return line feed pair cannot be found, then -1 will be
	 * returned to indicate that the data is not yet complete.
	 * 
	 * @return The value of the chunk length header, or -1 if more data is
	 *         required.
	 */
	private long getChunkLength() {
		// Look for a carriage return line feed pair.
		for (int i = buffer.readerIndex() + 1; i < buffer.writerIndex(); i++) {
			if (buffer.getByte(i) == 0x0A) {
				if (buffer.getByte(i - 1) == 0x0D) {
					// All data between the reader index and one before i is the header.
					String chunkSizeString = buffer.toString(buffer.readerIndex(), i - buffer.readerIndex() - 1,
							CharsetUtil.UTF_8);
					long chunkSize = Long.parseLong(chunkSizeString);

					// Move the buffer past the current reader index.
					buffer.readerIndex(i + 1);

					return chunkSize;
				}
			}
		}

		return -1;
	}


	private void initializeChunk() {
		try {
			tmpDataFile = File.createTempFile("change", ".tmp");
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to create replication data temp file", e);
		}
		try {
			tmpDataChannel = new FileOutputStream(tmpDataFile).getChannel();
		} catch (FileNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to open chunk data temp file", e);
		}
		
		chunkInProgress = true;
	}


	private void writeToChunk(ChannelBuffer writeBuffer) {
		try {
			// We can only write the minimum of the number of bytes available
			// and the number of bytes remaining. The bytes available is an
			// integer so if all values are possible the minimum value will also
			// be an integer.
			int bytesToWrite = (int) Math.min(writeBuffer.readableBytes(), bytesRemaining);
			
			// Write the data to the chunk data file.
			tmpDataChannel.write(writeBuffer.toByteBuffer(writeBuffer.readerIndex(), bytesToWrite));
			writeBuffer.skipBytes(bytesToWrite);
			
			bytesRemaining -= bytesToWrite;

		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to write chunk data to temp file", e);
		}
		
		// Complete the chunk if it is complete.
		if (bytesRemaining <= 0) {
			try {
				tmpDataChannel.close();
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to close chunk data temp file", e);
			}

			readyFiles.add(tmpDataFile);
			tmpDataFile = null;
			chunkInProgress = false;
		}
	}
	
	
	private List<File> createResultFileList() {
		List<File> resultFiles = new ArrayList<File>(readyFiles);
		readyFiles.clear();
		
		return resultFiles;
	}


	/**
	 * Processes the data in the input buffer. It requires each chunk of data to
	 * be preceeded by a length header. Once all data within each chunk has been
	 * read, a file will be produced which holds the contents of the chunk. When
	 * no more data is available, all files resulting from the input data will
	 * be returned. It is possible that 0 files will be returned. Any leftover
	 * data not comprising a complete chunk will be retained internally until
	 * the next call.
	 * 
	 * @param inputBuffer
	 *            Add data in this buffer will be added.
	 * @return All chunk data files completed during this call.
	 */
	public List<File> processData(ChannelBuffer inputBuffer) {
		while (inputBuffer.readableBytes() > 0 || buffer.readableBytes() > 0) {
			// If there is no chunk in progress so the next data will be the
			// header.
			if (!chunkInProgress) {
				
				// Move all input data to our internal buffer so that we have a
				// single view of all available data.
				buffer.writeBytes(inputBuffer);
				bytesRemaining = getChunkLength();
				
				if (bytesRemaining >= 0) {
					// We have read the chunk header, so now begin processing the
					// chunk body.
					initializeChunk();
				} else {
					return createResultFileList();
				}
			}
			
			// If we've reached this far we know a chunk is in progress so we
			// must write to the data file. We write from the internal buffer if
			// available, otherwise the input buffer.
			if (buffer.readableBytes() > 0) {
				writeToChunk(buffer);
			} else {
				writeToChunk(inputBuffer);
			}
		}
		
		return createResultFileList();
	}


	/**
	 * Gets the buffer.
	 * 
	 * @return The buffer.
	 */
	public ChannelBuffer getBuffer() {
		return buffer;
	}


	@Override
	public void release() {
		if (tmpDataChannel != null) {
			try {
				tmpDataChannel.close();
			} catch (IOException ex) {
				LOG.log(Level.WARNING, "Unable to close the current temporary chunk file", ex);
			}
		}
		
		if (tmpDataFile != null) {
			if (!tmpDataFile.delete()) {
				LOG.log(Level.WARNING, "Unable to delete the current temporary chunk file " + tmpDataFile);
			}
		}
		
		for (File readyFile : readyFiles) {
			if (!readyFile.delete()) {
				LOG.log(Level.WARNING, "Unable to delete the current temporary chunk file " + readyFile);
			}
		}
		readyFiles.clear();
	}
}
