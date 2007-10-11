package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.filter.common.BigBitSet;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkSource;


/**
 * A base class for all tasks filter entities within an area.
 * 
 * @author Brett Henderson
 */
public abstract class AreaFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private BigBitSet availableNodes;
	private BigBitSet availableWays;
	private BigBitSet availableRelations;
	
	
	/**
	 * Creates a new instance.
	 */
	public AreaFilter() {
		availableNodes = new BigBitSet();
		availableWays = new BigBitSet();
		availableRelations = new BigBitSet();
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
			availableNodes.set(nodeId);
			
			sink.process(container);
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
		
		// Only add node references for nodes that are within the bounding box.
		for (WayNode nodeReference : way.getWayNodeList()) {
			long nodeId;
			
			nodeId = nodeReference.getNodeId();
			
			if (availableNodes.get(nodeId)) {
				filteredWay.addWayNode(nodeReference);
			}
		}
		
		// Only add ways that contain nodes.
		if (filteredWay.getWayNodeList().size() > 0) {
			// Add all tags to the filtered node.
			for (Tag tag : way.getTagList()) {
				filteredWay.addTag(tag);
			}
			
			availableWays.set(way.getId());
			
			sink.process(new WayContainer(filteredWay));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation relation;
		Relation filteredRelation;
		
		relation = container.getEntity();
		
		// Create a new relation object to contain only items within the bounding box.
		filteredRelation = new Relation(relation.getId(), relation.getTimestamp(), relation.getUser());
		
		// Only add members for entities that are within the bounding box.
		for (RelationMember member : relation.getMemberList()) {
			long memberId;
			EntityType memberType;
			
			memberId = member.getMemberId();
			memberType = member.getMemberType();
			
			if (EntityType.Node.equals(memberType)) {
				if (availableNodes.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else if (EntityType.Way.equals(memberType)) {
				if (availableWays.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else if (EntityType.Relation.equals(memberType)) {
				if (availableRelations.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else {
				throw new OsmosisRuntimeException("Unsupported member type + " + memberType + " for relation " + relation.getId() + ".");
			}
		}
		
		// Only add relations that contain entities.
		if (filteredRelation.getMemberList().size() > 0) {
			// Add all tags to the filtered relation.
			for (Tag tag : relation.getTagList()) {
				filteredRelation.addTag(tag);
			}
			
			availableRelations.set(relation.getId());
			
			sink.process(new RelationContainer(filteredRelation));
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
