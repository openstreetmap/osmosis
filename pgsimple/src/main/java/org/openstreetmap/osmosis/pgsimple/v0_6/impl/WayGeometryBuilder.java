// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.pgsimple.common.CompactPersistentNodeLocationStore;
import org.openstreetmap.osmosis.pgsimple.common.InMemoryNodeLocationStore;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocation;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStore;
import org.openstreetmap.osmosis.pgsimple.common.NodeLocationStoreType;
import org.openstreetmap.osmosis.pgsimple.common.PersistentNodeLocationStore;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;


/**
 * Caches a set of node latitudes and longitudes and uses these to calculate the
 * geometries for ways.
 * 
 * @author Brett Henderson
 */
public class WayGeometryBuilder implements Releasable {
	/**
	 * Stores the locations of nodes so that they can be used to build the way
	 * geometries.
	 */
	protected NodeLocationStore locationStore;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeType
	 *            The type of storage to use for holding node locations.
	 */
	public WayGeometryBuilder(NodeLocationStoreType storeType) {
		if (NodeLocationStoreType.InMemory.equals(storeType)) {
			locationStore = new InMemoryNodeLocationStore();
		} else if (NodeLocationStoreType.TempFile.equals(storeType)) {
			locationStore = new PersistentNodeLocationStore();
		} else if (NodeLocationStoreType.CompactTempFile.equals(storeType)) {
			locationStore = new CompactPersistentNodeLocationStore();
		} else {
			throw new OsmosisRuntimeException("The store type " + storeType + " is not recognized.");
		}
	}
	
	
	/**
	 * Adds the location of the node to the internal store.
	 * 
	 * @param node
	 *            The node to add.
	 */
	public void addNodeLocation(Node node) {
		locationStore.addLocation(node.getId(), new NodeLocation(node.getLongitude(), node.getLatitude()));
	}

    /**
     * Get NodeLocation from internal store.
     *
     * @param nodeId
     *              Id of the node we want the location for.
     * @return Location of node
     */
    public NodeLocation getNodeLocation(long nodeId) {
        return locationStore.getNodeLocation(nodeId);
    }
	
	private Polygon createWayBbox(double left, double right, double bottom, double top) {
		Point[] points;
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
	
	
	/**
	 * Creates a linestring from a list of points.
	 * 
	 * @param points
	 *            The points making up the line.
	 * @return The linestring.
	 */
	public LineString createLinestring(List<Point> points) {
		LineString lineString;
		
		lineString = new LineString(points.toArray(new Point[]{}));
		lineString.srid = 4326;
		
		return lineString;
	}


    /**
     * @param nodeId
     *             Id of the node.
     * @return Point object
     */
    public Point createPoint(long nodeId) {
	    NodeLocation nodeLocation = locationStore.getNodeLocation(nodeId);
        Point point = new Point(nodeLocation.getLongitude(), nodeLocation.getLatitude());
        point.srid = 4326;

        return point;
    }

	
	/**
	 * Builds a bounding box geometry object from the node references in the
	 * specified way. Unknown nodes will be ignored.
	 * 
	 * @param way
	 *            The way to create the bounding box for.
	 * @return The bounding box surrounding the way.
	 */
	public Polygon createWayBbox(Way way) {
		double left;
		double right;
		double top;
		double bottom;
		boolean nodesFound;
		
		nodesFound = false;
		left = 0;
		right = 0;
		bottom = 0;
		top = 0;
		for (WayNode wayNode : way.getWayNodes()) {
			NodeLocation nodeLocation;
			double longitude;
			double latitude;
			
			nodeLocation = locationStore.getNodeLocation(wayNode.getNodeId());
			longitude = nodeLocation.getLongitude();
			latitude = nodeLocation.getLatitude();
			
			if (nodeLocation.isValid()) {
				if (nodesFound) {
					if (longitude < left) {
						left = longitude;
					}
					if (longitude > right) {
						right = longitude;
					}
					if (latitude < bottom) {
						bottom = latitude;
					}
					if (latitude > top) {
						top = latitude;
					}
				} else {
					left = longitude;
					right = longitude;
					bottom = latitude;
					top = latitude;
					
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
	public LineString createWayLinestring(Way way) {
		List<Point> linePoints;
		int numValidNodes = 0;
		
		linePoints = new ArrayList<Point>();
		for (WayNode wayNode : way.getWayNodes()) {
			NodeLocation nodeLocation;
			
			nodeLocation = locationStore.getNodeLocation(wayNode.getNodeId());
	
			if (nodeLocation.isValid()) {
				numValidNodes++;
				linePoints.add(new Point(nodeLocation.getLongitude(), nodeLocation.getLatitude()));
			} else {
				return null;
			}
		}
	
		if (numValidNodes >= 2) {	
			return createLinestring(linePoints);
		} else {
			return null;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		locationStore.release();
	}
}
