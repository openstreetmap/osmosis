// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.WayBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerFactory;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * A base class for all tasks filter entities within an area.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 */
public abstract class AreaFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private IdTracker availableNodes;
	private IdTracker requiredNodes; // Nodes needed to make complete Ways
	private SimpleObjectStore<NodeContainer> allNodes;
	private boolean clipIncompleteEntities;
	private boolean completeWays;
	private IdTracker availableWays;
	private SimpleObjectStore<WayContainer> allWays;
	private IdTracker availableRelations;
	private boolean completeRelations;
	private SimpleObjectStore<RelationContainer> allRelations;
	private WayBuilder wayBuilder;
	private RelationBuilder relationBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 * @param clipIncompleteEntities
	 *            If true, entities referring to non-existent entities will be
	 *            modified to ensure referential integrity. For example, ways
	 *            will be modified to only include nodes inside the area.
	 * @param completeWays
	 *            Include all nodes for ways which have at least one node inside
	 *            the filtered area.
	 * @param completeRelations
	 *            Include all relations referenced by other relations which have
	 *            members inside the filtered area.
	 */
	public AreaFilter(IdTrackerType idTrackerType, boolean clipIncompleteEntities, boolean completeWays, boolean completeRelations) {
		this.clipIncompleteEntities = clipIncompleteEntities;
		this.completeWays = completeWays;
		this.completeRelations = completeRelations;
		
		availableNodes = IdTrackerFactory.createInstance(idTrackerType);
		if (completeWays) {
			requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
			allNodes = new SimpleObjectStore<NodeContainer>(new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
			allWays = new SimpleObjectStore<WayContainer>(new SingleClassObjectSerializationFactory(WayContainer.class), "afwy", true);
		}
		availableWays = IdTrackerFactory.createInstance(idTrackerType);
		availableRelations = IdTrackerFactory.createInstance(idTrackerType);
		if (this.completeRelations || this.completeWays) {
			allRelations = new SimpleObjectStore<RelationContainer>(new SingleClassObjectSerializationFactory(RelationContainer.class), "afrl", true);
		}
		
		wayBuilder = new WayBuilder();
		relationBuilder = new RelationBuilder();
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
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
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
		
		// If complete ways are desired, stuff the node into a file
		if (completeWays) {
			allNodes.add(container);
		}
		// Only add the node if it lies within the box boundaries.
		if (isNodeWithinArea(node)) {
			availableNodes.set(nodeId);
			if (!completeWays) { // just pass it on immediately
				sink.process(container);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;
		
		way = container.getEntity();

		// First look through all the nodes to see if any are within the filtered area
		for (WayNode nodeReference : way.getWayNodes()) {
			if (availableNodes.get(nodeReference.getNodeId())) {
				availableWays.set(way.getId());
				break;
			}
		}
		
		// If the way has at least one node in the filtered area
		if (availableWays.get(way.getId())) {
			// If complete ways are desired, mark all its nodes as required, then stuff the way into
			// a file until all the ways are read
			if (completeWays) {
				for (WayNode nodeReference : way.getWayNodes()) {
					requiredNodes.set(nodeReference.getNodeId());
				}
				allWays.add(container);
			} else { // just filter it on the available nodes and pass it on
				emitFilteredWay(way);
			}
		} 
	}
	
	/**
	 * Construct a new way composed of only those nodes which are available and send it on to the
	 * sink.
	 * @param way
	 *            Complete way entity from which to filter out nodes which are not available.
	 */
	private void emitFilteredWay(Way way) {
		if (clipIncompleteEntities) {
			// Create a new way to contain only available nodes.
			wayBuilder.initialize(way);
			wayBuilder.clearWayNodes();
			
			// Only add node references for nodes that are available.
			for (WayNode nodeReference : way.getWayNodes()) {
				long nodeId;
				
				nodeId = nodeReference.getNodeId();
				
				if (availableNodes.get(nodeId)) {
					wayBuilder.addWayNode(nodeReference);
				}
			}
			
			// Only add ways that contain nodes.
			if (wayBuilder.getWayNodes().size() > 0) {
				sink.process(new WayContainer(wayBuilder.buildEntity()));
			}
			
		} else {
			sink.process(new WayContainer(way));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation relation;
		
		relation = container.getEntity();

		// If complete relations are wanted, then mark each one as available and stuff them
		// into a file for later retrieval. This has a potential referential integrity problem
		// in that if it is marked as available and referenced by another relation, but later
		// when it is read back, none of its members are available, the filtered relation will
		// be empty so it won't get passed on to the sink.
		if (completeRelations) {
			availableRelations.set(relation.getId());
		} else { // examine the relation to see if it should be passed on
			// First look through all the members to see if any are within the filtered area
			for (RelationMember member : relation.getMembers()) {
				long memberId;
				EntityType memberType;
				
				memberId = member.getMemberId();
				memberType = member.getMemberType();
				
				if ((EntityType.Node.equals(memberType) && availableNodes.get(memberId))
					|| (EntityType.Way.equals(memberType) && availableWays.get(memberId)) 
					|| (EntityType.Relation.equals(memberType) && availableRelations.get(memberId))) {
						availableRelations.set(relation.getId());
						break;
				}
			}
		}
		
		// if the relation is of interest
		if (availableRelations.get(relation.getId())) {
			// if the ways and nodes are being saved, or if complete relations are required 
			if (completeWays || completeRelations) {
				allRelations.add(container);
			} else { // just filter it on available members and pass it on to the sink
				emitFilteredRelation(relation);
			}
		}
	}


	/**
	 * Construct a new relation composed of only those entities which are available and send it on 
	 * to the sink.
     * @param relation
     *            Complete relation from which to filter out members which are not available.
     */
    private void emitFilteredRelation(Relation relation) {
    	if (clipIncompleteEntities) {
		    // Create a new relation object to contain only items within the bounding box.
    		relationBuilder.initialize(relation);
    		relationBuilder.clearMembers();
			
			// Only add members for entities that are available.
			for (RelationMember member : relation.getMembers()) {
				long memberId;
				EntityType memberType;
				
				memberId = member.getMemberId();
				memberType = member.getMemberType();
				
				if (EntityType.Node.equals(memberType)) {
					if (availableNodes.get(memberId)) {
						relationBuilder.addMember(member);
					}
				} else if (EntityType.Way.equals(memberType)) {
					if (availableWays.get(memberId)) {
						relationBuilder.addMember(member);
					}
				} else if (EntityType.Relation.equals(memberType)) {
					if (availableRelations.get(memberId)) {
						relationBuilder.addMember(member);
					}
				} else {
					throw new OsmosisRuntimeException("Unsupported member type + " + memberType + " for relation " + relation.getId() + ".");
				}
			}
			
			// Only add relations that contain entities.
			if (relationBuilder.getMembers().size() > 0) {
				sink.process(new RelationContainer(relationBuilder.buildEntity()));
			}
			
    	} else {
    		sink.process(new RelationContainer(relation));
    	}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<NodeContainer> nodeIterator;
		ReleasableIterator<WayContainer> wayIterator;
		ReleasableIterator<RelationContainer> relationIterator;
		NodeContainer nodeContainer;
		long nodeId;
		if (completeWays) {
			// first send on all the nodes
			nodeIterator = allNodes.iterate();
			while (nodeIterator.hasNext()) {
				nodeContainer = nodeIterator.next();
				nodeId = nodeContainer.getEntity().getId();
				// Send on all the stored nodes which are available (in the filtered area) or
				// required (to make a way complete) 
				if (availableNodes.get(nodeId) || requiredNodes.get(nodeId)) {
					availableNodes.set(nodeId); // make sure to mark it as available for ways
					sink.process(nodeContainer);
				}
			}
			// 
			nodeIterator.release();
			nodeIterator = null;
			// next send on all the ways
			wayIterator = allWays.iterate();
			while (wayIterator.hasNext()) {
				emitFilteredWay(wayIterator.next().getEntity());
			}
			wayIterator.release();
			wayIterator = null;
		}
		if (completeWays || completeRelations) {
			relationIterator = allRelations.iterate();
			while (relationIterator.hasNext()) {
				emitFilteredRelation(relationIterator.next().getEntity());
			}
		}
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (allNodes != null) {
			allNodes.release();
		}
		if (allWays != null) {
			allWays.release();			
		}
		if (allRelations != null) {
			allRelations.release();
		}
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
