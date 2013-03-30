// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.bound.v0_6;

import java.util.Date;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.testutil.v0_6.SinkEntityInspector;

/**
 * Test for the bound setter task.
 *
 * @author Igor Podolskiy
 */
public class BoundSetterTest {

	/**
	 * Tests the bound removal.
	 */
	@Test
	public void removeExistingBoundTest() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundSetter setter = new BoundSetter(null);
		setter.setSink(inspector);
		setter.process(new BoundContainer(new Bound("Test")));
		setter.process(new NodeContainer(new Node(
				new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		setter.complete();
		setter.release();
		
		EntityContainer ec = inspector.getProcessedEntities().iterator().next();
		Assert.assertEquals(EntityType.Node, ec.getEntity().getType());
	}
	
	/**
	 * Tests the bound removal when there is no bound upstream.
	 */
	@Test
	public void removeNoBoundTest() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundSetter setter = new BoundSetter(null);
		setter.setSink(inspector);
		setter.process(new NodeContainer(new Node(
				new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		setter.complete();
		setter.release();
		
		EntityContainer ec = inspector.getProcessedEntities().iterator().next();
		Assert.assertEquals(EntityType.Node, ec.getEntity().getType());
	}
	
	/**
	 * Tests the bound setting.
	 */
	@Test
	public void overwriteBoundTest() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		Bound newBound = new Bound(2, 1, 4, 3, "NewBound");
		BoundSetter setter = new BoundSetter(newBound);
		setter.setSink(inspector);
		setter.process(new BoundContainer(new Bound("Test")));
		setter.process(new NodeContainer(new Node(
				new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		setter.complete();
		setter.release();
		
		Iterator<EntityContainer> iterator = inspector.getProcessedEntities().iterator();
		EntityContainer ec = iterator.next();
		Assert.assertEquals(EntityType.Bound, ec.getEntity().getType());
		Bound bound = (Bound) ec.getEntity();
		Assert.assertEquals(bound, newBound);
		
		// Ensure there is no second bound
		ec = iterator.next();
		Assert.assertEquals(EntityType.Node, ec.getEntity().getType());
	}
	
	/**
	 * Tests the bound setting when there is no bound upstream.
	 */
	@Test
	public void setNewBoundTest() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		Bound newBound = new Bound(2, 1, 4, 3, "NewBound");
		BoundSetter setter = new BoundSetter(newBound);
		setter.setSink(inspector);
		setter.process(new NodeContainer(new Node(
				new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		setter.complete();
		setter.release();
		
		EntityContainer ec = inspector.getProcessedEntities().iterator().next();
		Assert.assertEquals(EntityType.Bound, ec.getEntity().getType());
		Bound bound = (Bound) ec.getEntity();
		Assert.assertEquals(bound, newBound);
	}

}
