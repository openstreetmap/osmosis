// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * Verifies that read-only entities can be cloned.
 * 
 * @author Brett Henderson
 * 
 */
public class CloneTest {
	/**
	 * Node cloning test.
	 */
	@Test
	public void testNodeClone() {
		// Build the original entity.
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("myKey", "myValue"));
		Node entity = new Node(new CommonEntityData(1, 2, new Date(0), OsmUser.NONE, 3, tags), 4, 5);

		// Cloning a writeable object should return the original object.
		Assert.assertSame("Entity was cloned", entity, entity.getWriteableInstance());

		// Get a cloned entity.
		entity.makeReadOnly();
		Node clonedEntity = entity.getWriteableInstance();

		// Make sure we weren't assigned the original entity.
		Assert.assertNotSame("Entity was not cloned", entity, clonedEntity);
	}


	/**
	 * Way cloning test.
	 */
	@Test
	public void testWayClone() {
		// Build the original entity.
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("myKey", "myValue"));
		List<WayNode> wayNodes = new ArrayList<WayNode>();
		wayNodes.add(new WayNode(1));
		Way entity = new Way(new CommonEntityData(1, 2, new Date(0), OsmUser.NONE, 3, tags), wayNodes);

		// Cloning a writeable object should return the original object.
		Assert.assertSame("Entity was cloned", entity, entity.getWriteableInstance());

		// Get a cloned entity.
		entity.makeReadOnly();
		Way clonedEntity = entity.getWriteableInstance();

		// Make sure we weren't assigned the original entity.
		Assert.assertNotSame("Entity was not cloned", entity, clonedEntity);
	}


	/**
	 * Relation cloning test.
	 */
	@Test
	public void testRelationClone() {
		// Build the original entity.
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("myKey", "myValue"));
		List<RelationMember> members = new ArrayList<RelationMember>();
		members.add(new RelationMember(1, EntityType.Node, "myRole"));
		Relation entity = new Relation(new CommonEntityData(1, 2, new Date(0), OsmUser.NONE, 3, tags), members);

		// Cloning a writeable object should return the original object.
		Assert.assertSame("Entity was cloned", entity, entity.getWriteableInstance());

		// Get a cloned entity.
		entity.makeReadOnly();
		Relation clonedEntity = entity.getWriteableInstance();

		// Make sure we weren't assigned the original entity.
		Assert.assertNotSame("Entity was not cloned", entity, clonedEntity);
	}
}
