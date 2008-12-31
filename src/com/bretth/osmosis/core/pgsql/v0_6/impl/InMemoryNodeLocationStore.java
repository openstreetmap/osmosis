package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * An in-memory node location store implementation.
 * 
 * @author Brett Henderson
 */
public class InMemoryNodeLocationStore implements NodeLocationStore {
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
	 * Writes the specified integer to a buffer.
	 * 
	 * @param value
	 *            The integer to write.
	 * @param buffer
	 *            The destination buffer.
	 * @param initialOffset
	 *            The buffer offset to begin writing at.
	 */
	private void writeIntToBuffer(int value, byte buffer[], int initialOffset) {
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
	private int readIntFromBuffer(byte buffer[], int initialOffset) {
		int offset;
		
		offset = initialOffset;
		
		return (
			buffer[offset++] << 24) +
			((buffer[offset++] & 0xFF) << 16) +
			((buffer[offset++] & 0xFF) << 8) +
			(buffer[offset++] & 0xFF);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLocation(long nodeId, NodeLocation nodeLocation) {
		int bufferIndex;
		byte buffer[];
		int bufferOffset;
		
		bufferIndex = (int)(nodeId / BUFFER_ELEMENT_COUNT);
		
		while (bufferIndex >= buffers.size()) {
			buffer = new byte[BUFFER_SIZE];
			
			buffers.add(buffer);
		}
		
		buffer = buffers.get(bufferIndex);
		
		bufferOffset = (int) ((nodeId - (bufferIndex * BUFFER_ELEMENT_COUNT)) * NODE_DATA_SIZE);
		
		buffer[bufferOffset++] = 1;
		writeIntToBuffer(FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLongitude()), buffer, bufferOffset);
		bufferOffset += 4;
		writeIntToBuffer(FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLatitude()), buffer, bufferOffset);
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
		
		bufferIndex = (int)(nodeId / BUFFER_ELEMENT_COUNT);
		
		if (bufferIndex < buffers.size()) {
			byte buffer[];
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
		// Do nothing.
	}
}
