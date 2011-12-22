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
 * Unit tests for the bound computer.
 * 
 * @author Igor Podolskiy
 */
public class BoundComputerTest {

	/**
	 * Tests the bound computation if no nodes are upstream.
	 */
	@Test
	public void computeBoundNoNodes() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundComputer bc = new BoundComputer("NewBound");
		bc.setSink(inspector);
		bc.complete();
		bc.release();

		Assert.assertNull(inspector.getLastEntityContainer());
	}


	/**
	 * Tests the bound computation if no nodes but a bound entity is upstream.
	 */
	@Test
	public void computeBoundNoNodesWithBound() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundComputer bc = new BoundComputer("NewBound");
		bc.setSink(inspector);
		bc.process(new BoundContainer(new Bound("Test")));
		bc.complete();
		bc.release();

		Assert.assertNull(inspector.getLastEntityContainer());
	}


	/**
	 * Tests the bound computation when no bound entity is upstream.
	 */
	@Test
	public void computeBoundNoUpstreamBound() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundComputer bc = new BoundComputer("NewBound");
		bc.setSink(inspector);
		bc.process(new NodeContainer(new Node(new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		bc.process(new NodeContainer(new Node(new CommonEntityData(2, 2, new Date(), OsmUser.NONE, 1), 2, 2)));
		bc.complete();
		bc.release();

		EntityContainer ec = inspector.getProcessedEntities().iterator().next();
		Assert.assertEquals(new Bound(2, 1, 2, 1, "NewBound"), ec.getEntity());
	}


	/**
	 * Tests the bound computation when there is bound entity is upstream.
	 */
	@Test
	public void computeBoundWithUpstreamBound() {
		SinkEntityInspector inspector = new SinkEntityInspector();
		BoundComputer bc = new BoundComputer("NewBound");
		bc.setSink(inspector);
		bc.process(new NodeContainer(new Node(new CommonEntityData(1, 1, new Date(), OsmUser.NONE, 1), 1, 1)));
		bc.process(new NodeContainer(new Node(new CommonEntityData(2, 2, new Date(), OsmUser.NONE, 1), 2, 2)));
		bc.complete();
		bc.release();

		Iterator<EntityContainer> iterator = inspector.getProcessedEntities().iterator();
		EntityContainer ec = iterator.next();
		Assert.assertEquals(new Bound(2, 1, 2, 1, "NewBound"), ec.getEntity());

		// Ensure there is no second bound.
		ec = iterator.next();
		Assert.assertEquals(EntityType.Node, ec.getEntity().getType());
	}

}
