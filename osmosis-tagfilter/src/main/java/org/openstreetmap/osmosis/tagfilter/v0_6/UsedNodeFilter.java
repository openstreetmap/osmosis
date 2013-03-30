// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
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
 * Restricts output of nodes to those that are used in ways and relations.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 * @author Christoph Sommer 
 * @author Bartosz Fabianowski
 */
public class UsedNodeFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private SimpleObjectStore<NodeContainer> allNodes;
	private SimpleObjectStore<WayContainer> allWays;
	private SimpleObjectStore<RelationContainer> allRelations;
	private IdTracker requiredNodes;
	
	
	/**
	 * Creates a new instance.
	 *
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 */
	public UsedNodeFilter(IdTrackerType idTrackerType) {
		allNodes = new SimpleObjectStore<NodeContainer>(
				new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
		allWays = new SimpleObjectStore<WayContainer>(
				new SingleClassObjectSerializationFactory(WayContainer.class), "afwy", true);
		allRelations = new SimpleObjectStore<RelationContainer>(
				new SingleClassObjectSerializationFactory(RelationContainer.class), "afrl", true);

		requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
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
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		allNodes.add(container);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;

		// mark all nodes as required		
		way = container.getEntity();
		for (WayNode nodeReference : way.getWayNodes()) {
			long nodeId = nodeReference.getNodeId();
			requiredNodes.set(nodeId);
		}

		allWays.add(container);

	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation relation;

		// mark all nodes as required
		relation = container.getEntity();
		for (RelationMember memberReference : relation.getMembers()) {
			if (memberReference.getMemberType() == EntityType.Node) {
				long nodeId = memberReference.getMemberId();
				requiredNodes.set(nodeId);
			}
		}

		allRelations.add(container);
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {

		// send on all required nodes
		ReleasableIterator<NodeContainer> nodeIterator = allNodes.iterate();
		while (nodeIterator.hasNext()) {
			NodeContainer nodeContainer = nodeIterator.next();
			long nodeId = nodeContainer.getEntity().getId();
			if (!requiredNodes.get(nodeId)) {
				continue;
			}
			sink.process(nodeContainer);
		}
		nodeIterator.release();
		nodeIterator = null;

		// send on all ways
		ReleasableIterator<WayContainer> wayIterator = allWays.iterate();
		while (wayIterator.hasNext()) {
			sink.process(wayIterator.next());
		}
		wayIterator.release();
		wayIterator = null;

		// send on all relations
		ReleasableIterator<RelationContainer> relationIterator = allRelations.iterate();
		while (relationIterator.hasNext()) {
			sink.process(relationIterator.next());
		}
		relationIterator.release();
		relationIterator = null;

		// done
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
