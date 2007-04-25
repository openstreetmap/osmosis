package com.bretth.osm.conduit.filter;

import java.util.BitSet;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.OsmSink;
import com.bretth.osm.conduit.task.OsmTransformer;


public class BoundingBoxFilter implements OsmTransformer {
	private OsmSink osmSink;
	private double left;
	private double right;
	private double top;
	private double bottom;
	private BitSet availableNodes;
	private BitSet availableSegments;
	
	
	public BoundingBoxFilter(double left, double right, double top, double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		
		availableNodes = new BitSet();
		availableSegments = new BitSet();
	}
	
	
	public void addNode(Node node) {
		long nodeId;
		double latitude;
		double longitude;
		
		nodeId = node.getId();
		latitude = node.getLatitude();
		longitude = node.getLongitude();
		
		// Only add the node if it lies within the box boundaries.
		if (top >= latitude && bottom <= latitude && left <= longitude && right >= longitude) {
			osmSink.addNode(node);
			
			// Ensure that the node identifier can be represented as an integer.
			if (nodeId > Integer.MAX_VALUE) {
				throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
			}
			
			availableNodes.set((int) nodeId);
		}
	}
	
	
	public void addSegment(Segment segment) {
		long segmentId;
		long nodeIdFrom;
		long nodeIdTo;
		
		segmentId = segment.getId();
		nodeIdFrom = segment.getFrom();
		nodeIdTo = segment.getTo();
		
		// Ensure that all identifiers can be represented as integers.
		if (segmentId > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdFrom > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdTo > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		
		// Only add the segment if at least one of its nodes are within the bounding box.
		if (availableNodes.get((int) nodeIdFrom) || availableNodes.get((int) nodeIdTo)) {
			osmSink.addSegment(segment);
			availableSegments.set((int) segmentId);
		}
	}
	
	
	public void addWay(Way way) {
		Way filteredWay;
		
		// Create a new way object to contain only items within the bounding box.
		filteredWay = new Way(way.getId(), way.getTimestamp());
		
		// Only add segment references to segments that aren't within the bounding box.
		for (SegmentReference segmentReference : way.getSegmentReferenceList()) {
			long segmentId;
			
			segmentId = segmentReference.getSegmentId();
			
			// Ensure that the segment identifier can be represented as an integer.
			if (segmentId > Integer.MAX_VALUE) {
				throw new ConduitRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
			}
			
			
			if (availableSegments.get((int) segmentId)) {
				filteredWay.addSegmentReference(segmentReference);
			}
		}
		
		// Only add ways that contain segments.
		if (filteredWay.getSegmentReferenceList().size() > 0) {
			// Add all tags to the filtered node.
			for (Tag tag : way.getTagList()) {
				filteredWay.addTag(tag);
			}
			
			osmSink.addWay(way);
		}
	}
	
	
	public void complete() {
		osmSink.complete();
	}
	
	
	public void release() {
		osmSink.release();
	}
	
	
	public void setOsmSink(OsmSink osmSink) {
		this.osmSink = osmSink;
	}
}
