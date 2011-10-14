// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.store.BufferedRandomAccessFileInputStream;
import org.openstreetmap.osmosis.core.store.StorageStage;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * A file-based node location store implementation.
 * 
 * @author Brett Henderson
 */
public class PersistentNodeLocationStore implements NodeLocationStore {
	
	private static final Logger LOG = Logger.getLogger(PersistentNodeLocationStore.class.getName());
	private static final int ZERO_BUFFER_SIZE = 1024 * 1024;
	private static final int NODE_DATA_SIZE = 9;
	
	private File nodeStorageFile;
	private StorageStage stage;
	private long lastNodeId;
	private FileOutputStream fileOutStream;
	private DataOutputStream dataOutStream;
	private BufferedRandomAccessFileInputStream fileInStream;
	private DataInputStream dataInStream;
	private long currentFileOffset;
	private byte[] zeroBuffer;
	private NodeLocation invalidNodeLocation;
	
	
	/**
	 * Creates a new instance.
	 */
	public PersistentNodeLocationStore() {
		stage = StorageStage.NotStarted;
		
		lastNodeId = Long.MIN_VALUE;
		
		zeroBuffer = new byte[ZERO_BUFFER_SIZE];
		Arrays.fill(zeroBuffer, (byte) 0);
		
		invalidNodeLocation = new NodeLocation();
	}
	
	
	private void initializeAddStage() {
		// We can't add if we've passed the add stage.
		if (stage.compareTo(StorageStage.Add) > 0) {
			throw new OsmosisRuntimeException("Cannot add to storage in stage " + stage + ".");
		}
		
		// If we're not up to the add stage, initialise for adding.
		if (stage.compareTo(StorageStage.Add) < 0) {
			try {
				nodeStorageFile = File.createTempFile("nodelatlon", null);
				
				fileOutStream = new FileOutputStream(nodeStorageFile);
				dataOutStream = new DataOutputStream(new BufferedOutputStream(fileOutStream, 65536));
				currentFileOffset = 0;
				
				stage = StorageStage.Add;
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException(
						"Unable to create object stream writing to temporary file " + nodeStorageFile + ".", e);
			}
		}
	}
	
	
	private void initializeReadingStage() {
		// If we're already in the reading stage, do nothing.
		if (stage.compareTo(StorageStage.Reading) == 0) {
			return;
		}
		
		// If we've been released, we can't iterate.
		if (stage.compareTo(StorageStage.Released) >= 0) {
			throw new OsmosisRuntimeException("Cannot read from node storage in stage " + stage + ".");
		}
		
		// If no data was written, writing should be initialized before reading.
		if (stage.compareTo(StorageStage.NotStarted) <= 0) {
			initializeAddStage();
		}
		
		// If we're in the add stage, close the output streams.
		if (stage.compareTo(StorageStage.Add) == 0) {
			try {
				dataOutStream.close();
				fileOutStream.close();
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to close output stream.", e);
			} finally {
				dataOutStream = null;
				fileOutStream = null;
			}
			
			try {
				fileInStream = new BufferedRandomAccessFileInputStream(nodeStorageFile);
				dataInStream = new DataInputStream(fileInStream);
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open the node data file " + nodeStorageFile + ".", e);
			}
			
			stage = StorageStage.Reading;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLocation(long nodeId, NodeLocation nodeLocation) {
		long requiredFileOffset;
		
		initializeAddStage();
		
		// We can only add nodes in sorted order.
		if (nodeId <= lastNodeId) {
			throw new OsmosisRuntimeException(
				"The node id of " + nodeId
				+ " must be greater than the previous id of "
				+ lastNodeId + "."
			);
		}
		lastNodeId = nodeId;
		
		try {
			// Write zeros to the file where no node data is available.
			requiredFileOffset = nodeId * NODE_DATA_SIZE;
			if (requiredFileOffset > currentFileOffset) {
				while (currentFileOffset < requiredFileOffset) {
					long offsetDifference;
					
					offsetDifference = requiredFileOffset - currentFileOffset;
					if (offsetDifference > ZERO_BUFFER_SIZE) {
						offsetDifference = ZERO_BUFFER_SIZE;
					}
					
					dataOutStream.write(zeroBuffer, 0, (int) offsetDifference);
					
					currentFileOffset += offsetDifference;
				}
			}
			
			// Write the node data. Prefix with a non-zero byte to identify that
			// data is available for this node.
			dataOutStream.writeByte(1);
			dataOutStream.writeInt(FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLongitude()));
			dataOutStream.writeInt(FixedPrecisionCoordinateConvertor.convertToFixed(nodeLocation.getLatitude()));
			currentFileOffset += NODE_DATA_SIZE;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
				"Unable to write node location data to node storage file "
					+ nodeStorageFile + ".",
				e
			);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeLocation getNodeLocation(long nodeId) {
		NodeLocation nodeLocation;
		long offset;
		
		initializeReadingStage();
		
		offset = nodeId * NODE_DATA_SIZE;
		
		nodeLocation = invalidNodeLocation;
		
		if (offset < currentFileOffset) {
			try {
				byte validFlag;
				
				fileInStream.seek(offset);
				validFlag = dataInStream.readByte();
				
				if (validFlag != 0) {
					nodeLocation = new NodeLocation(
						FixedPrecisionCoordinateConvertor.convertToDouble(dataInStream.readInt()),
						FixedPrecisionCoordinateConvertor.convertToDouble(dataInStream.readInt())
					); 
				}
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to read node information from the node storage file.", e);
			}
		}
		
		return nodeLocation;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (fileOutStream != null) {
			try {
				fileOutStream.close();
			} catch (Exception e) {
				// We cannot throw an exception within a release method.
				LOG.log(Level.WARNING, "Unable to close file output stream.", e);
			}
			fileOutStream = null;
		}
		
		if (fileInStream != null) {
			try {
				fileInStream.close();
			} catch (Exception e) {
				// We cannot throw an exception within a release method.
				LOG.log(Level.WARNING, "Unable to close file input stream.", e);
			}
			fileInStream = null;
		}
		
		if (nodeStorageFile != null) {
			if (!nodeStorageFile.delete()) {
				// We cannot throw an exception within a release method.
				LOG.warning("Unable to delete file " + nodeStorageFile);
			}
			nodeStorageFile = null;
		}
	}
}
