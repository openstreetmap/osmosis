package com.bretth.osmosis.core.filter.v0_4;

import java.util.BitSet;

import com.bretth.osmosis.core.container.v0_4.EntityContainer;
import com.bretth.osmosis.core.container.v0_4.EntityProcessor;
import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_4.Node;
import com.bretth.osmosis.core.domain.v0_4.Segment;
import com.bretth.osmosis.core.domain.v0_4.SegmentReference;
import com.bretth.osmosis.core.domain.v0_4.Tag;
import com.bretth.osmosis.core.domain.v0_4.Way;
import com.bretth.osmosis.core.task.v0_4.Sink;
import com.bretth.osmosis.core.task.v0_4.SinkSource;


/**
 * A base class for all tasks filter entities within an area.
 * 
 * @author Brett Henderson
 */
public abstract class AreaFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private BitSet availableNodes;
	private BitSet availableSegments;
	
	
	/**
	 * Creates a new instance.
	 */
	public AreaFilter() {
		availableNodes = new BitSet();
		availableSegments = new BitSet();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}
	
	
	/**
	 * Indicates if the node lies within the area required.
	 * 
	 * @param node
	 *            The node to be checked.
	 * @return True if the node lies within the area.
	 */
	protected abstract boolean isNodeWithinArea(Node node);
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		Node node;
		long nodeId;
		
		node = container.getEntity();
		nodeId = node.getId();
		
		// Only add the node if it lies within the box boundaries.
		if (isNodeWithinArea(node)) {
			sink.process(container);
			
			// Ensure that the node identifier can be represented as an integer.
			if (nodeId > Integer.MAX_VALUE) {
				throw new OsmosisRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
			}
			
			availableNodes.set((int) nodeId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(SegmentContainer container) {
		Segment segment;
		long segmentId;
		long nodeIdFrom;
		long nodeIdTo;
		
		segment = container.getEntity();
		segmentId = segment.getId();
		nodeIdFrom = segment.getFrom();
		nodeIdTo = segment.getTo();
		
		// Ensure that all identifiers can be represented as integers.
		if (segmentId > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdFrom > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdTo > Integer.MAX_VALUE) {
			throw new OsmosisRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		
		// Only add the segment if both of its nodes are within the bounding box.
		if (availableNodes.get((int) nodeIdFrom) && availableNodes.get((int) nodeIdTo)) {
			sink.process(container);
			availableSegments.set((int) segmentId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;
		Way filteredWay;
		
		way = container.getEntity();
		
		// Create a new way object to contain only items within the bounding box.
		filteredWay = new Way(way.getId(), way.getTimestamp(), way.getUser());
		
		// Only add segment references to segments that are within the bounding box.
		for (SegmentReference segmentReference : way.getSegmentReferenceList()) {
			long segmentId;
			
			segmentId = segmentReference.getSegmentId();
			
			// Ensure that the segment identifier can be represented as an integer.
			if (segmentId > Integer.MAX_VALUE) {
				throw new OsmosisRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
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
			
			sink.process(new WayContainer(filteredWay));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
