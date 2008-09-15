package com.bretth.osmosis.core.migrate;

import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;


/**
 * A task for converting 0.5 data into 0.6 format.  This isn't a true migration but okay for most uses.
 * 
 * @author Brett Henderson
 */
public class MigrateV05ToV06 implements Sink05Source06, EntityProcessor {
	
	private com.bretth.osmosis.core.task.v0_6.Sink sink;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer bound) {
		sink.process(new com.bretth.osmosis.core.container.v0_6.BoundContainer(new Bound(bound.getEntity().getOrigin())));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer node) {
		Node oldNode;
		com.bretth.osmosis.core.domain.v0_6.Node newNode;
		
		oldNode = node.getEntity();
		newNode = new com.bretth.osmosis.core.domain.v0_6.Node(
			oldNode.getId(),
			1,
			oldNode.getTimestamp(),
			OsmUser.NONE,
			oldNode.getLatitude(),
			oldNode.getLongitude()
		);
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.NodeContainer(newNode));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer way) {
		Way oldWay;
		com.bretth.osmosis.core.domain.v0_6.Way newWay;
		
		oldWay = way.getEntity();
		newWay = new com.bretth.osmosis.core.domain.v0_6.Way(
			oldWay.getId(),
			1,
			oldWay.getTimestamp(),
			OsmUser.NONE
		);
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.WayContainer(newWay));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relation) {
		Relation oldRelation;
		com.bretth.osmosis.core.domain.v0_6.Relation newRelation;
		
		oldRelation = relation.getEntity();
		newRelation = new com.bretth.osmosis.core.domain.v0_6.Relation(
			oldRelation.getId(),
			1,
			oldRelation.getTimestamp(),
			OsmUser.NONE
		);
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.RelationContainer(newRelation));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(com.bretth.osmosis.core.task.v0_6.Sink sink) {
		this.sink = sink;
	}
}
