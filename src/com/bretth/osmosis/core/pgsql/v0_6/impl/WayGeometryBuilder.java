package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.store.BufferedRandomAccessFileInputStream;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.StorageStage;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Caches a set of node latitudes and longitudes and uses these to calculate the
 * geometries for ways.
 * 
 * @author Brett Henderson
 */
public class WayGeometryBuilder implements Releasable {
	
	private static final Logger log = Logger.getLogger(WayGeometryBuilder.class.getName());
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
	
	
	/**
	 * Creates a new instance.
	 */
	public WayGeometryBuilder() {
		stage = StorageStage.NotStarted;
		
		lastNodeId = Long.MIN_VALUE;
		
		zeroBuffer = new byte[ZERO_BUFFER_SIZE];
		Arrays.fill(zeroBuffer, (byte) 0);
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
				throw new OsmosisRuntimeException("Unable to create object stream writing to temporary file " + nodeStorageFile + ".", e);
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
	 * Adds the location of the node to the internal store.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addNodeLocation(Node node) {
		long nodeId;
		long requiredFileOffset;
		
		initializeAddStage();
		
		// We can only add nodes in sorted order.
		nodeId = node.getId();
		if (nodeId <= lastNodeId) {
			throw new OsmosisRuntimeException(
				"The node id of " + nodeId +
				" must be greater than the previous id of " +
				lastNodeId + "."
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
			dataOutStream.writeInt(FixedPrecisionCoordinateConvertor.convertToFixed(node.getLongitude()));
			dataOutStream.writeInt(FixedPrecisionCoordinateConvertor.convertToFixed(node.getLatitude()));
			currentFileOffset += NODE_DATA_SIZE;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException(
				"Unable to write node location data to node storage file " +
				nodeStorageFile + ".",
				e
			);
		}
	}
	
	
	private Geometry createWayBbox(double left, double right, double bottom, double top) {
		Point points[];
		LinearRing ring;
		Polygon bbox;
		
		points = new Point[5];
		points[0] = new Point(left, bottom);
		points[1] = new Point(left, top);
		points[2] = new Point(right, top);
		points[3] = new Point(right, bottom);
		points[4] = new Point(left, bottom);
		
		ring = new LinearRing(points);
		
		bbox = new Polygon(new LinearRing[] {ring});
		bbox.srid = 4326;
		
		return bbox;
	}
	
	
	private Geometry createLinestring(List<Point> points) {
		LineString lineString;
		
		lineString = new LineString(points.toArray(new Point[]{}));
		lineString.srid = 4326;
		
		return lineString;
	}


	/**
	 * Retrieves node location information from the temporary data file.
	 * 
	 * @param nodeId
	 *            The id of the node to retrieve.
	 * @return The node location details.
	 */
	private NodeLocation getNodeLocation(long nodeId) {
		NodeLocation nodeLocation;
		long offset;
		
		offset = nodeId * NODE_DATA_SIZE;
		
		nodeLocation = new NodeLocation();
		
		if (offset < currentFileOffset) {
			try {
				byte validFlag;
				
				fileInStream.seek(offset);
				validFlag = dataInStream.readByte();
				
				if (validFlag != 0) {
					nodeLocation.validFlag = true;
					nodeLocation.longitude = FixedPrecisionCoordinateConvertor.convertToDouble(dataInStream.readInt());
					nodeLocation.latitude = FixedPrecisionCoordinateConvertor.convertToDouble(dataInStream.readInt());
				}
				
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to read node information from the node storage file.", e);
			}
		}
		
		return nodeLocation;
	}
	
	
	/**
	 * Builds a bounding box geometry object from the node references in the
	 * specified way. Unknown nodes will be ignored.
	 * 
	 * @param way
	 *            The way to create the bounding box for.
	 * @return The bounding box surrounding the way.
	 */
	public Geometry createWayBbox(Way way) {
		double left;
		double right;
		double top;
		double bottom;
		boolean nodesFound;
		
		initializeReadingStage();
		
		nodesFound = false;
		left = 0;
		right = 0;
		bottom = 0;
		top = 0;
		for (WayNode wayNode : way.getWayNodeList()) {
			NodeLocation nodeLocation;
			
			nodeLocation = getNodeLocation(wayNode.getNodeId());
			
			if (nodeLocation.validFlag) {
				if (nodesFound) {
					if (nodeLocation.longitude < left) {
						left = nodeLocation.longitude;
					}
					if (nodeLocation.longitude > right) {
						right = nodeLocation.longitude;
					}
					if (nodeLocation.latitude < bottom) {
						bottom = nodeLocation.latitude;
					}
					if (nodeLocation.latitude > top) {
						top = nodeLocation.latitude;
					}
				} else {
					left = nodeLocation.longitude;
					right = nodeLocation.longitude;
					bottom = nodeLocation.latitude;
					top = nodeLocation.latitude;
					
					nodesFound = true;
				}
			}
		}
		
		return createWayBbox(left, right, bottom, top);
	}
	
	
	/**
	 * Builds a linestring geometry object from the node references in the
	 * specified way. Unknown nodes will be ignored.
	 * 
	 * @param way
	 *            The way to create the linestring for.
	 * @return The linestring representing the way.
	 */
	public Geometry createWayLinestring(Way way) {
		List<Point> linePoints;
		
		initializeReadingStage();
		
		linePoints = new ArrayList<Point>();
		for (WayNode wayNode : way.getWayNodeList()) {
			NodeLocation nodeLocation;
			
			nodeLocation = getNodeLocation(wayNode.getNodeId());
			
			if (nodeLocation.validFlag) {
				linePoints.add(new Point(nodeLocation.longitude, nodeLocation.latitude));
			}
		}
		
		return createLinestring(linePoints);
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
				// Do nothing.
			}
			fileOutStream = null;
		}
		
		if (fileInStream != null) {
			try {
				fileInStream.close();
			} catch (Exception e) {
				// Do nothing.
			}
			fileInStream = null;
		}
		
		if (nodeStorageFile != null) {
			if (!nodeStorageFile.delete()) {
				log.warning("Unable to delete file " + nodeStorageFile);
			}
			nodeStorageFile = null;
		}
	}
	
	
	/**
	 * Data structure containing node location information.
	 * 
	 * @author Brett Henderson
	 */
	private static class NodeLocation {
		/**
		 * Creates a new instance.
		 */
		public NodeLocation() {
			validFlag = false;
		}
		
		public boolean validFlag;
		public double latitude;
		public double longitude;
	}
}
