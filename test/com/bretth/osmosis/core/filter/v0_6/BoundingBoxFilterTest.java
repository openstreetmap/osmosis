// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_6;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.test.task.v0_6.SinkEntityInspector;

/**
 * @author Karl Newman
 * 
 */
public class BoundingBoxFilterTest {

	private SinkEntityInspector entityInspector;
	private AreaFilter simpleAreaFilter;
	private Bound intersectingBound;
	private Bound nonIntersectingBound;
	private Node inAreaNode;
	private Node outOfAreaNode;
	private Node edgeNodeEast;
	private Node edgeNodeWest;
	private Node edgeNodeNorth;
	private Node edgeNodeSouth;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		entityInspector = new SinkEntityInspector();
		// simpleAreaFilter doesn't cross antimeridian; no complete ways or relations
		simpleAreaFilter = new BoundingBoxFilter(
		        IdTrackerType.IdList,
		        -20,
		        20,
		        20,
		        -20,
		        false,
		        false);
		simpleAreaFilter.setSink(entityInspector);
		intersectingBound = new Bound(30, 10, 30, 10, "intersecting");
		nonIntersectingBound = new Bound(-30, -50, 10, -10, "nonintersecting");
		inAreaNode = new Node(1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), 10, 10);
		outOfAreaNode = new Node(1235, 0, new Date(), new OsmUser(12, "OsmosisTest"), 30, 30);
		edgeNodeEast = new Node(1236, 0, new Date(), new OsmUser(12, "OsmosisTest"), 10, 20);
		edgeNodeWest = new Node(1237, 0, new Date(), new OsmUser(12, "OsmosisTest"), 10, -20);
		edgeNodeNorth = new Node(1238, 0, new Date(), new OsmUser(12, "OsmosisTest"), 20, 10);
		edgeNodeSouth = new Node(1239, 0, new Date(), new OsmUser(12, "OsmosisTest"), -20, 10);
	}


	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		simpleAreaFilter.release();
	}


	/**
	 * Test passing a Bound which intersects the filter area.
	 */
	@Test
	public final void testProcessBoundContainer1() {
		Bound compareBound;
		simpleAreaFilter.process(new BoundContainer(intersectingBound));
		simpleAreaFilter.complete();
		compareBound = (Bound)entityInspector.getLastEntityContainer().getEntity();
		assertTrue((Double.compare(compareBound.getRight(), 20) == 0)
		        && (Double.compare(compareBound.getLeft(), 10) == 0)
		        && (Double.compare(compareBound.getTop(), 20) == 0)
		        && (Double.compare(compareBound.getBottom(), 10) == 0)
		        && compareBound.getOrigin().equals("intersecting"));
	}


	/**
	 * Test the non-passing of a Bound which does not intersect the filter area.
	 */
	@Test
	public final void testProcessBoundContainer2() {
		simpleAreaFilter.process(new BoundContainer(nonIntersectingBound));
		simpleAreaFilter.complete();
		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test a node inside the area.
	 */
	@Test
	public final void testIsNodeWithinArea1() {
		assertTrue(
		        "Node lying inside filter area not considered inside area",
		        simpleAreaFilter.isNodeWithinArea(inAreaNode));
	}


	/**
	 * Test a node outside the area.
	 */
	@Test
	public final void testIsNodeWithinArea2() {
		assertFalse(
		        "Node lying outside filter area not considered outside area",
		        simpleAreaFilter.isNodeWithinArea(outOfAreaNode));
	}


	/**
	 * Test a node on the East edge of the area.
	 */
	@Test
	public final void testIsNodeWithinArea3() {
		assertTrue(
		        "Node lying on East edge of filter area not considered inside area",
		        simpleAreaFilter.isNodeWithinArea(edgeNodeEast));
	}


	/**
	 * Test a node on the West edge of the area.
	 */
	@Test
	public final void testIsNodeWithinArea4() {
		assertTrue(
		        "Node lying on West edge of filter area not considered inside area",
		        simpleAreaFilter.isNodeWithinArea(edgeNodeWest));
	}


	/**
	 * Test a node on the North edge of the area.
	 */
	@Test
	public final void testIsNodeWithinArea5() {
		assertTrue(
		        "Node lying on North edge of filter area not considered inside area",
		        simpleAreaFilter.isNodeWithinArea(edgeNodeNorth));
	}


	/**
	 * Test a node on the South edge of the area.
	 */
	@Test
	public final void testIsNodeWithinArea6() {
		assertTrue(
		        "Node lying on South edge of filter area not considered inside area",
		        simpleAreaFilter.isNodeWithinArea(edgeNodeSouth));
	}
}
