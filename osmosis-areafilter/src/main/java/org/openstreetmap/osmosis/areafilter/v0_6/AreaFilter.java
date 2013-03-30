// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.areafilter.v0_6;

import java.util.Iterator;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
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
	private IdTracker availableNodes; // Nodes within the area.
	private IdTracker requiredNodes; // Nodes needed to complete referencing entities.
	private IdTracker availableWays; // Ways within the area.
	private IdTracker requiredWays; // Ways needed to complete referencing relations.
	private IdTracker availableRelations; // Relations within the area.
	private IdTracker requiredRelations; // Relations needed to complete referencing relations.
	private boolean clipIncompleteEntities;
	private boolean completeWays;
	private boolean completeRelations;
	private boolean storeEntities;
    private boolean cascadingRelations;
	private SimpleObjectStore<WayContainer> allWays;
	private SimpleObjectStore<NodeContainer> allNodes;
    // this duplicates as a container for held-back relations in the cascadingRelations case:
	private SimpleObjectStore<RelationContainer> allRelations; 
	
	
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
	 * @param cascadingRelations
	 *            Make sure that a relation referencing a relation which is included
	 *            will also be included.
	 */
	public AreaFilter(
			IdTrackerType idTrackerType, boolean clipIncompleteEntities, boolean completeWays,
			boolean completeRelations, boolean cascadingRelations) {
		this.clipIncompleteEntities = clipIncompleteEntities;
		// Allowing complete relations without complete ways is very difficult and not allowed for
		// now.
		this.completeWays = completeWays || completeRelations;
		this.completeRelations = completeRelations;
        // cascadingRelations is included for free with any of the complete options so you don't
        // need it if those are set.
        this.cascadingRelations = cascadingRelations && !completeRelations && !completeWays;
		
		availableNodes = IdTrackerFactory.createInstance(idTrackerType);
		requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
		availableWays = IdTrackerFactory.createInstance(idTrackerType);
		requiredWays = IdTrackerFactory.createInstance(idTrackerType);
		availableRelations = IdTrackerFactory.createInstance(idTrackerType);
		requiredRelations = IdTrackerFactory.createInstance(idTrackerType);
		
		// If either complete ways or complete relations are required, then all data must be stored
		// during processing.
		storeEntities = completeWays || completeRelations;
		if (storeEntities) {
			allNodes = new SimpleObjectStore<NodeContainer>(
					new SingleClassObjectSerializationFactory(NodeContainer.class), "afn", true);
			allWays = new SimpleObjectStore<WayContainer>(
					new SingleClassObjectSerializationFactory(WayContainer.class), "afw", true);
			allRelations =
				new SimpleObjectStore<RelationContainer>(
						new SingleClassObjectSerializationFactory(RelationContainer.class), "afr", true);
		} else if (cascadingRelations) {
            allRelations = 
				new SimpleObjectStore<RelationContainer>(
						new SingleClassObjectSerializationFactory(RelationContainer.class), "afr", true);
        }
	}


    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
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
		
		node = container.getEntity();
		
		// Check if we're storing entities for later.
		if (storeEntities) {
			allNodes.add(container);
		}
		
		// Only add the node if it lies within the box boundaries.
		if (isNodeWithinArea(node)) {
			availableNodes.set(node.getId());
			
			// If we're not storing entities, we pass it on immediately.
			if (!storeEntities) {
				emitNode(container);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;
		boolean inArea;
		
		way = container.getEntity();

		// Check if we're storing entities for later.
		if (storeEntities) {
			allWays.add(container);
		}
		
		// First look through all the nodes to see if any are within the filtered area
		inArea = false;
		for (WayNode nodeReference : way.getWayNodes()) {
			if (availableNodes.get(nodeReference.getNodeId())) {
				inArea = true;
				break;
			}
		}
		
		// If the way has at least one node in the filtered area.
		if (inArea) {
			availableWays.set(way.getId());
			
			// If complete ways are desired, mark any unavailable nodes as required.
			if (completeWays) {
				for (WayNode nodeReference : way.getWayNodes()) {
					long nodeId = nodeReference.getNodeId();
					
					if (!availableNodes.get(nodeId)) {
						requiredNodes.set(nodeId);
					}
				}
			}
			
			// If we're not storing entities, we pass it on immediately.
			if (!storeEntities) {
				emitWay(container);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation relation;
		boolean inArea;
        boolean holdBackRelation;
		
		relation = container.getEntity();
		
		// First look through all the node and way members to see if any are within the filtered area
		inArea = false;
        holdBackRelation = false;

		for (RelationMember member : relation.getMembers()) {
			switch (member.getMemberType()) {
			case Node:
				inArea = availableNodes.get(member.getMemberId());
				break;
			case Way:
				inArea = availableWays.get(member.getMemberId());
				break;
			case Relation:
				inArea = availableRelations.get(member.getMemberId());
				break;
			default:
				break;
			}
			
			if (inArea) {
				break;
			}
		}

        if (cascadingRelations) { //  && referencesOtherRelation && (!inArea || clipIncompleteEntities)) {
            holdBackRelation = true;
        }

		// Check if we're storing entities for later.
		if (storeEntities || holdBackRelation) {
			allRelations.add(container);
        }
		
		// If the relation has at least one member in the filtered area.
		if (inArea) {
			availableRelations.set(relation.getId());
			
			// If we're not storing entities, we pass it on immediately.
			if (!storeEntities && !holdBackRelation) {
				emitRelation(container);
			}
		}
	}


	/**
	 * Sends a node to the sink. This will perform any necessary transformations on the node before
	 * sending it.
	 * 
	 * @param nodeContainer
	 *            Node to be sent.
	 */
	private void emitNode(NodeContainer nodeContainer) {
		sink.process(nodeContainer);
	}


	/**
	 * Sends a way to the sink. This will perform any necessary transformations on the way before
	 * sending it.
	 * 
	 * @param wayContainer
	 *            Way to be sent.
	 */
	private void emitWay(WayContainer wayContainer) {
		if (clipIncompleteEntities) {
			WayContainer filteredWayContainer;
			Way filteredWay;
			
			filteredWayContainer = wayContainer.getWriteableInstance();
			filteredWay = filteredWayContainer.getEntity();
			
			// Remove node references for nodes that are unavailable.
			for (Iterator<WayNode> i = filteredWay.getWayNodes().iterator(); i.hasNext();) {
				WayNode nodeReference = i.next();
				
				if (!availableNodes.get(nodeReference.getNodeId())) {
					i.remove();
				}
			}
			
			// Only add ways that contain nodes.
			if (filteredWay.getWayNodes().size() > 0) {
				sink.process(filteredWayContainer);
			}
			
		} else {
			sink.process(wayContainer);
		}
	}


	/**
	 * Sends a relation to the sink. This will perform any necessary transformations on the way before
	 * sending it.
	 * 
	 * @param relationContainer
	 *            Relation to be sent.
	 */
	private void emitRelation(RelationContainer relationContainer) {
    	if (clipIncompleteEntities) {
    		RelationContainer filteredRelationContainer;
    		Relation filteredRelation;
    		
		    filteredRelationContainer = relationContainer.getWriteableInstance();
		    filteredRelation = filteredRelationContainer.getEntity();
		    
		    // Remove members for entities that are unavailable.
		    for (Iterator<RelationMember> i = filteredRelation.getMembers().iterator(); i.hasNext();) {
		    	RelationMember member = i.next();
		    	EntityType memberType;
		    	long memberId;
		    	
		    	memberType = member.getMemberType();
		    	memberId = member.getMemberId();
		    	
		    	switch (memberType) {
		    	case Node:
		    		if (!availableNodes.get(memberId)) {
						i.remove();
					}
		    		break;
		    	case Way:
		    		if (!availableWays.get(memberId)) {
						i.remove();
					}
		    		break;
		    	case Relation:
		    		if (!availableRelations.get(memberId)) {
						i.remove();
					}
		    		break;
		    	default:
		    			break;
		    	}
		    }
			
			// Only add relations that contain entities.
			if (filteredRelation.getMembers().size() > 0) {
				sink.process(filteredRelationContainer);
			}
			
    	} else {
    		sink.process(relationContainer);
    	}
	}
	
	
	private boolean selectParentRelationsPass() {
		ReleasableIterator<RelationContainer> i = allRelations.iterate();
		
		try {
			int selectionCount;
			
			selectionCount = 0;
			
			while (i.hasNext()) {
				Relation relation = i.next().getEntity();
				long relationId = relation.getId();
				
				// Ignore relations that have already been selected.
				if (!availableRelations.get(relationId)) {
					
					// This relation becomes an available relation if one of its member
					// relations is also available.
					for (RelationMember member : relation.getMembers()) {
						if (member.getMemberType().equals(EntityType.Relation)) {
							if (availableRelations.get(member.getMemberId())) {
								availableRelations.set(relationId);
								selectionCount++;
							}
						}
					}
				}
			}
			
			return selectionCount > 0;
			
		} finally {
			i.release();
		}
	}


	/**
	 * Walk up the relation tree. This means iterating through relations until all parent relations
	 * of existing relations are marked in the available list. We may have to do this multiple times
	 * depending on the nesting level of relations.
	 */
	private void selectParentRelations() {
		boolean selectionsMade;
		
		do {
			selectionsMade = selectParentRelationsPass();
		} while (selectionsMade);
	}


	/**
	 * Select all relation members of type relation for existing selected relations. This may need
	 * to be called several times until all children are selected.
	 * 
	 * @return True if additional selections were made an another pass is needed.
	 */
	private boolean selectChildRelationsPass() {
		ReleasableIterator<RelationContainer> i = allRelations.iterate();
		
		try {
			int selectionCount;
			
			selectionCount = 0;
			
			while (i.hasNext()) {
				Relation relation = i.next().getEntity();
				long relationId = relation.getId();
				
				// Only examine available relations.
				if (availableRelations.get(relationId)) {
					// Select the child if it hasn't already been selected.
					for (RelationMember member : relation.getMembers()) {
						if (member.getMemberType().equals(EntityType.Relation)) {
							long memberId = member.getMemberId();
							
							if (!availableRelations.get(memberId)) {
								availableRelations.set(memberId);
								selectionCount++;
							}
						}
					}
				}
			}
			
			return selectionCount > 0;
			
		} finally {
			i.release();
		}
	}
	
	
	/**
	 * Select all relation members of type node or way for existing selected relations.
	 */
	private void selectChildNonRelationsPass() {
		ReleasableIterator<RelationContainer> i = allRelations.iterate();
		
		try {
			while (i.hasNext()) {
				Relation relation = i.next().getEntity();
				long relationId = relation.getId();
				
				// Only examine available relations.
				if (availableRelations.get(relationId)) {
					// Select the member if it hasn't already been selected.
					for (RelationMember member : relation.getMembers()) {
						switch (member.getMemberType()) {
						case Node:
							availableNodes.set(member.getMemberId());
							break;
						case Way:
							availableWays.set(member.getMemberId());
							break;
						default:
							break;
						}
					}
				}
			}
			
		} finally {
			i.release();
		}
	}
	
	
	/**
	 * Select all nodes within already selected ways.
	 */
	private void selectWayNodes() {
		ReleasableIterator<WayContainer> i = allWays.iterate();
		
		try {
			while (i.hasNext()) {
				Way way = i.next().getEntity();
				long wayId = way.getId();
				
				// Only examine available relations.
				if (availableWays.get(wayId)) {
					// Select all nodes within the way.
					for (WayNode wayNode : way.getWayNodes()) {
						availableNodes.set(wayNode.getNodeId());
					}
				}
			}
			
		} finally {
			i.release();
		}
	}
	
	
	private void buildCompleteRelations() {
		boolean selectionsMade;
		
		// Select all child relation members of type relation. 
		do {
			selectionsMade = selectChildRelationsPass();
		} while (selectionsMade);
		
		// Select all child relation members of type way or node.
		selectChildNonRelationsPass();
		
		// Select all way nodes of existing nodes.
		selectWayNodes();
	}
    
    
    private void pumpNodesToSink() {
    	ReleasableIterator<NodeContainer> i = allNodes.iterate();
    	
    	try {
    		while (i.hasNext()) {
				NodeContainer nodeContainer = i.next();
				if (availableNodes.get(nodeContainer.getEntity().getId())) {
					emitNode(nodeContainer);
				}
			}
    		
    	} finally {
    		i.release();
    	}
    }
    
    
    private void pumpWaysToSink() {
    	ReleasableIterator<WayContainer> i = allWays.iterate();
    	
    	try {
    		while (i.hasNext()) {
				WayContainer wayContainer = i.next();
				if (availableWays.get(wayContainer.getEntity().getId())) {
					emitWay(wayContainer);
				}
			}
    		
    	} finally {
    		i.release();
    	}
    }
    
    
    private void pumpRelationsToSink() {
    	ReleasableIterator<RelationContainer> i = allRelations.iterate();
    	
    	try {
    		while (i.hasNext()) {
				RelationContainer relationContainer = i.next();
				if (availableRelations.get(relationContainer.getEntity().getId())) {
					emitRelation(relationContainer);
				}
			}
    		
    	} finally {
    		i.release();
    	}
    }
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		// If we've stored entities temporarily, we now need to forward the selected ones to the output.
		if (storeEntities) {
			// Select all parents of current relations.
			selectParentRelations();
			
			// Merge required ids into available ids.
			availableNodes.setAll(requiredNodes);
			availableWays.setAll(requiredWays);
			availableRelations.setAll(requiredRelations);
			requiredNodes = null;
			requiredWays = null;
			requiredRelations = null;
			
			if (completeRelations) {
				buildCompleteRelations();
			}
			
			// Send the selected entities to the output.
			pumpNodesToSink();
			pumpWaysToSink();
			pumpRelationsToSink();
		} else if (cascadingRelations) {
			// Select all parents of current relations.
			selectParentRelations();
			availableRelations.setAll(requiredRelations);
			
			// nodes, ways, and relations *not* referencing other relations will already have
            // been written in this mode. we only pump the remaining ones, relations that 
            // reference other relations. this may result in an un-ordered relation stream.
			pumpRelationsToSink();
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
