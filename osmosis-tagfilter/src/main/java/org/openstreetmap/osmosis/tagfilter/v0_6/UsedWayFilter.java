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
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerFactory;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Restricts output of ways to those that are used in relations.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 * @author Christoph Sommer 
 * @author Bartosz Fabianowski
 */
public class UsedWayFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private SimpleObjectStore<NodeContainer> allNodes;
	private SimpleObjectStore<WayContainer> allWays;
	private SimpleObjectStore<RelationContainer> allRelations;
	private IdTracker requiredWays;
	
	
	/**
	 * Creates a new instance.
	 *
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 */
	public UsedWayFilter(IdTrackerType idTrackerType) {
		allNodes = new SimpleObjectStore<NodeContainer>(
				new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
		allWays = new SimpleObjectStore<WayContainer>(
				new SingleClassObjectSerializationFactory(WayContainer.class), "afwy", true);
		allRelations = new SimpleObjectStore<RelationContainer>(
				new SingleClassObjectSerializationFactory(RelationContainer.class), "afrl", true);

		requiredWays = IdTrackerFactory.createInstance(idTrackerType);
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
			if (memberReference.getMemberType() == EntityType.Way) {
				long wayId = memberReference.getMemberId();
				requiredWays.set(wayId);
			}
		}

		allRelations.add(container);
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {
    // send on all nodes
    ReleasableIterator<NodeContainer> nodeIterator = allNodes.iterate();
    while (nodeIterator.hasNext()) {
      sink.process(nodeIterator.next());
    }
    nodeIterator.release();
    nodeIterator = null;

		// send on all required ways
		ReleasableIterator<WayContainer> wayIterator = allWays.iterate();
		while (wayIterator.hasNext()) {
			WayContainer wayContainer = wayIterator.next();
			long wayId = wayContainer.getEntity().getId();
			if (!requiredWays.get(wayId)) {
				continue;
			}
			sink.process(wayContainer);
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
