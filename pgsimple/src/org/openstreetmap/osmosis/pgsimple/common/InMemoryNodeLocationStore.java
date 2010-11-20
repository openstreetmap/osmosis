// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * An in-memory node location store implementation.
 * 
 * @author Brett Henderson
 */
public class InMemoryNodeLocationStore implements NodeLocationStore {
	private static final Logger LOG = Logger.getLogger(InMemoryNodeLocationStore.class.getName());
	
	private static final int NODE_DATA_SIZE = 9;
	private static final int BUFFER_ELEMENT_COUNT = 131072;
	private static final int BUFFER_SIZE = NODE_DATA_SIZE * BUFFER_ELEMENT_COUNT;
	
	
	private List<byte[]> buffers;
	private NodeLocation invalidNodeLocation;
	
	
	/**
	 * Creates a new instance.
	 */
	public InMemoryNodeLocationStore() {
		buffers = new ArrayList<byte[]>();
		
		invalidNodeLocation = new NodeLocation();
	}

	/**
	 * Writes a summary of the current memory consumption at the specified
	 * logging level.
	 * 
	 * @param level
	 *            The logging level to write the summary at.
	 */
	private void logMemoryConsumption(Level level) {
		if (LOG.isLoggable(level)) {
			Runtime runtime;
			long totalUsed;
			double percentageUsed;
			long maxMemory;
			DecimalFormat percentageFormat;
			
			runtime = Runtime.getRuntime();
			percentageFormat = new DecimalFormat("#0.##");
			
			// Calculate the percentage of memory currently used.
			percentageUsed = ((double) runtime.totalMemory()) / runtime.maxMemory() * 100;
			totalUsed = ((long) buffers.size()) * BUFFER_SIZE / 1048576;
			maxMemory = runtime.maxMemory() / 1048576;
			
			LOG.log(
				level,
				"The store contains " + buffers.size()
				+ " buffers of " + (BUFFER_SIZE / 1024) + "KB, total "
				+ totalUsed + "MB.");
			LOG.log(
				level,
				"The JVM is using " + percentageFormat.format(percentageUsed)
				+ "% of the maximum " + maxMemory
				+ "MB of memory.");
		}
	}
	
	
	/**
	 * Writes the specified integer to a buffer.
	 * 
	 * @param value
	 *            The integer to write.
	 * @param buffer
	 *            The destination buffer.
	 * @param initialOffset
	 *            The buffer offset to begin writing at.
	 */
	private void writeIntToBuffer(int value, byte[] buffer, int initialOffset) {
		int offset;
		
		offset = initialOffset;
		
		buffer[offset++] = (byte) (value >>> 24);
		buffer[offset++] = (byte) (value >>> 16);
		buffer[offset++] = (byte) (value >>> 8);
		buffer[offset++] = (byte) value;
	}
	
	
	/**
	 * Reads an integer from a buffer.
	 * 
	 * @param buffer
	 *            The buffer to read from.
	 * @param initialOffset
	 *            The buffer offset to begin reading from.
	 * @return The integer.
	 */
	private int readIntFromBuffer(byte[] buffer, int initialOffset) {
		int offset;
		
		offset = initialOffset;
		
		return (
			buffer[offset++] << 24)
			+ ((buffer[offset++] & 0xFF) << 16)
			+ ((buffer[offset++] & 0xFF) << 8)
			+ (buffer[offset++] & 0xFF);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLocation(long nodeId, NodeLocation nodeLocation) {
		int bufferIndex;
		byte[] buffer;
		int bufferOffset;
		
		bufferIndex = (int) (nodeId / BUFFER_ELEMENT_COUNT);
		
		while (bufferIndex >= buffers.size()) {
			buffer = new byte[BUFFER_SIZE];
			
			buffers.add(buffer);
			
			logMemoryConsumption(Level.FINER);
		}
		
		buffer = buffers.get(bufferIndex);
		
		bufferOffset = (int) ((nodeId - (bufferIndex * BUFFER_ELEMENT_COUNT)) * NODE_DATA_SIZE);
		
		buffer[bufferOffset++] = 1;
		writeIntToBuffer(
				FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLongitude()), buffer, bufferOffset);
		bufferOffset += 4;
		writeIntToBuffer(
				FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLatitude()), buffer, bufferOffset);
		bufferOffset += 4;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeLocation getNodeLocation(long nodeId) {
		NodeLocation nodeLocation;
		int bufferIndex;
		
		nodeLocation = invalidNodeLocation;
		
		bufferIndex = (int) (nodeId / BUFFER_ELEMENT_COUNT);
		
		if (bufferIndex < buffers.size()) {
			byte[] buffer;
			int bufferOffset;
			byte validFlag;
			
			buffer = buffers.get(bufferIndex);
			
			bufferOffset = (int) ((nodeId - (bufferIndex * BUFFER_ELEMENT_COUNT)) * NODE_DATA_SIZE);
			
			validFlag = buffer[bufferOffset++];
			
			if (validFlag != 0) {
				int longitude;
				int latitude;
				
				longitude = readIntFromBuffer(buffer, bufferOffset);
				bufferOffset += 4;
				latitude = readIntFromBuffer(buffer, bufferOffset);
				bufferOffset += 4;
				
				nodeLocation = new NodeLocation(
					FixedPrecisionCoordinateConvertor.convertToDouble(longitude),
					FixedPrecisionCoordinateConvertor.convertToDouble(latitude)
				);
			}
		}
		
		return nodeLocation;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		logMemoryConsumption(Level.FINE);
	}
}
