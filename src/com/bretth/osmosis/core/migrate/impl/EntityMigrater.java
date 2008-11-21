package com.bretth.osmosis.core.migrate.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.WayNode;


/**
 * Provides conversion routines for entity classes between 0.5 and 0.6 format.
 * 
 * @author Brett Henderson
 */
public class EntityMigrater {
	
	private OsmUser migrateUser(com.bretth.osmosis.core.domain.v0_5.OsmUser user) {
		if (!user.equals(com.bretth.osmosis.core.domain.v0_5.OsmUser.NONE)) {
			return new OsmUser(user.getId(), user.getName());
		} else {
			return OsmUser.NONE;
		}
	}
	
	
	private List<Tag> migrateTags(com.bretth.osmosis.core.domain.v0_5.Entity entity) {
		List<com.bretth.osmosis.core.domain.v0_5.Tag> oldTags;
		List<Tag> newTags;
		
		oldTags = entity.getTagList();
		newTags = new ArrayList<Tag>(oldTags.size());
		
		for (com.bretth.osmosis.core.domain.v0_5.Tag oldTag : oldTags) {
			newTags.add(new Tag(oldTag.getKey(), oldTag.getValue()));
		}
		
		return newTags;
	}
	
	
	private List<WayNode> migrateWayNodes(com.bretth.osmosis.core.domain.v0_5.Way way) {
		List<com.bretth.osmosis.core.domain.v0_5.WayNode> oldWayNodes;
		List<WayNode> newWayNodes;
		
		oldWayNodes = way.getWayNodeList();
		newWayNodes = new ArrayList<WayNode>(oldWayNodes.size());
		
		for (com.bretth.osmosis.core.domain.v0_5.WayNode oldWayNode : oldWayNodes) {
			newWayNodes.add(new WayNode(oldWayNode.getNodeId()));
		}
		
		return newWayNodes;
	}
	
	
	private List<RelationMember> migrateRelationMembers(com.bretth.osmosis.core.domain.v0_5.Relation relation) {
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
	 * Migrates a bound object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Bound migrate(com.bretth.osmosis.core.domain.v0_5.Bound entity) {
		return new Bound(entity.getOrigin());
	}
	
	
	/**
	 * Migrates a node object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Node migrate(com.bretth.osmosis.core.domain.v0_5.Node entity) {
		Node newEntity;
		
		newEntity = new Node(
				entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser()),
			entity.getLatitude(),
			entity.getLongitude()
		);
		newEntity.addTags(migrateTags(entity));
		
		return newEntity;
	}
	
	
	/**
	 * Migrates a way object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Way migrate(com.bretth.osmosis.core.domain.v0_5.Way entity) {
		Way newEntity;
		
		newEntity = new Way(
			entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser())
		);
		newEntity.addTags(migrateTags(entity));
		newEntity.addWayNodes(migrateWayNodes(entity));
		
		return newEntity;
	}
	
	
	/**
	 * Migrates a relation object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Relation migrate(com.bretth.osmosis.core.domain.v0_5.Relation entity) {
		Relation newEntity;
		
		newEntity = new Relation(
			entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser())
		);
		newEntity.addTags(migrateTags(entity));
		newEntity.addMembers(migrateRelationMembers(entity));
		
		return newEntity;
	}
}
