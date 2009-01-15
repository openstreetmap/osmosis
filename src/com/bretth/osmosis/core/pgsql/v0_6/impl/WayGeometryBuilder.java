package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.lifecycle.Releasable;


/**
 * Caches a set of node latitudes and longitudes and uses these to calculate the
 * geometries for ways.
 * 
 * @author Brett Henderson
 */
public class WayGeometryBuilder implements Releasable {
	private NodeLocationStore locationStore;
	
	
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
		
		nodesFound = false;
		left = 0;
		right = 0;
		bottom = 0;
		top = 0;
		for (WayNode wayNode : way.getWayNodeList()) {
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
	public Geometry createWayLinestring(Way way) {
		List<Point> linePoints;
		int numValidNodes = 0;
		
		linePoints = new ArrayList<Point>();
		for (WayNode wayNode : way.getWayNodeList()) {
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
