package com.bretth.osmosis.core.migrate;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.Entity;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.WayNode;


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
	
	
	private OsmUser migrateUser(com.bretth.osmosis.core.domain.v0_5.OsmUser user) {
		if (!user.equals(com.bretth.osmosis.core.domain.v0_5.OsmUser.NONE)) {
			return new OsmUser(user.getId(), user.getName());
		} else {
			return OsmUser.NONE;
		}
	}
	
	
	private List<Tag> migrateTags(Entity entity) {
		List<com.bretth.osmosis.core.domain.v0_5.Tag> oldTags;
		List<Tag> newTags;
		
		oldTags = entity.getTagList();
		newTags = new ArrayList<Tag>(oldTags.size());
		
		for (com.bretth.osmosis.core.domain.v0_5.Tag oldTag : oldTags) {
			newTags.add(new Tag(oldTag.getKey(), oldTag.getValue()));
		}
		
		return newTags;
	}
	
	
	private List<WayNode> migrateWayNodes(Way way) {
		List<com.bretth.osmosis.core.domain.v0_5.WayNode> oldWayNodes;
		List<WayNode> newWayNodes;
		
		oldWayNodes = way.getWayNodeList();
		newWayNodes = new ArrayList<WayNode>(oldWayNodes.size());
		
		for (com.bretth.osmosis.core.domain.v0_5.WayNode oldWayNode : oldWayNodes) {
			newWayNodes.add(new WayNode(oldWayNode.getNodeId()));
		}
		
		return newWayNodes;
	}
	
	
	private List<RelationMember> migrateRelationMembers(Relation relation) {
		List<com.bretth.osmosis.core.domain.v0_5.RelationMember> oldRelationMembers;
		List<RelationMember> newRelationMembers;
		
		oldRelationMembers = relation.getMemberList();
		newRelationMembers = new ArrayList<RelationMember>(oldRelationMembers.size());
		
		for (com.bretth.osmosis.core.domain.v0_5.RelationMember oldRelationMember : oldRelationMembers) {
			newRelationMembers.add(
				new RelationMember(
					oldRelationMember.getMemberId(),
					EntityType.valueOf(oldRelationMember.getMemberType().name()),
					oldRelationMember.getMemberRole()
				)
			);
		}
		
		return newRelationMembers;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer node) {
		Node oldEntity;
		com.bretth.osmosis.core.domain.v0_6.Node newEntity;
		
		oldEntity = node.getEntity();
		newEntity = new com.bretth.osmosis.core.domain.v0_6.Node(
			oldEntity.getId(),
			1,
			oldEntity.getTimestamp(),
			migrateUser(oldEntity.getUser()),
			oldEntity.getLatitude(),
			oldEntity.getLongitude()
		);
		newEntity.addTags(migrateTags(oldEntity));
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.NodeContainer(newEntity));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer way) {
		Way oldEntity;
		com.bretth.osmosis.core.domain.v0_6.Way newEntity;
		
		oldEntity = way.getEntity();
		newEntity = new com.bretth.osmosis.core.domain.v0_6.Way(
			oldEntity.getId(),
			1,
			oldEntity.getTimestamp(),
			migrateUser(oldEntity.getUser())
		);
		newEntity.addTags(migrateTags(oldEntity));
		newEntity.addWayNodes(migrateWayNodes(oldEntity));
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.WayContainer(newEntity));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relation) {
		Relation oldEntity;
		com.bretth.osmosis.core.domain.v0_6.Relation newEntity;
		
		oldEntity = relation.getEntity();
		newEntity = new com.bretth.osmosis.core.domain.v0_6.Relation(
			oldEntity.getId(),
			1,
			oldEntity.getTimestamp(),
			migrateUser(oldEntity.getUser())
		);
		newEntity.addTags(migrateTags(oldEntity));
		newEntity.addMembers(migrateRelationMembers(oldEntity));
		
		sink.process(new com.bretth.osmosis.core.container.v0_6.RelationContainer(newEntity));
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
