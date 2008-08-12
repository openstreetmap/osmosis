// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_6;

import static org.junit.Assert.*;

import java.io.File;
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

public class PolygonFilterTest {

	File polygonFile;
	private SinkEntityInspector entityInspector;
	private AreaFilter polyAreaFilter;
	private Bound intersectingBound;
	private Bound crossingIntersectingBound;
	private Bound nonIntersectingBound;
	private Node inAreaNode;
	private Node outOfAreaNode;
	private Node edgeNode;


	@Before
	public void setUp() throws Exception {
		polygonFile = new File("test/com/bretth/osmosis/core/filter/v0_6/testPolygon.txt");
		entityInspector = new SinkEntityInspector();
		// polyAreaFilter has a notch out of the Northeast corner.
		polyAreaFilter = new PolygonFilter(IdTrackerType.IdList, polygonFile, false, false);
		polyAreaFilter.setSink(entityInspector);
		intersectingBound = new Bound(30, 0, 30, 0, "intersecting");
		crossingIntersectingBound = new Bound(-10, 10, 30, -30, "crossing intersecting");
		nonIntersectingBound = new Bound(30, 15, 30, 15, "nonintersecting");
		inAreaNode = new Node(1234, new Date(), new OsmUser("OsmosisTest", 12), 0, 5, 10);
		outOfAreaNode = new Node(1235, new Date(), new OsmUser("OsmosisTest", 12), 0, 15, 15);
		edgeNode = new Node(1236, new Date(), new OsmUser("OsmosisTest", 12), 0, 15, 10);
	}


	@After
	public void tearDown() throws Exception {
		polyAreaFilter.release();
	}


	/**
	 * Test passing a Bound which intersects the filter area.
	 */
	@Test
	public final void testProcessBoundContainer1() {
		Bound compareBound;
		polyAreaFilter.process(new BoundContainer(intersectingBound));
		polyAreaFilter.complete();
		compareBound = (Bound)entityInspector.getLastEntityContainer().getEntity();
		assertTrue((Double.compare(compareBound.getRight(), 20) == 0)
		        && (Double.compare(compareBound.getLeft(), 0) == 0)
		        && (Double.compare(compareBound.getTop(), 20) == 0)
		        && (Double.compare(compareBound.getBottom(), 0) == 0)
		        && compareBound.getOrigin().equals("intersecting"));
	}


	/**
	 * Test passing a Bound which crosses the antimeredian and intersects the filter area.
	 */
	@Test
	public final void testProcessBoundContainer2() {
		Bound compareBound;
		polyAreaFilter.process(new BoundContainer(crossingIntersectingBound));
		polyAreaFilter.complete();
		compareBound = (Bound)entityInspector.getLastEntityContainer().getEntity();
		assertTrue((Double.compare(compareBound.getRight(), 20) == 0)
		        && (Double.compare(compareBound.getLeft(), -20) == 0)
		        && (Double.compare(compareBound.getTop(), 20) == 0)
		        && (Double.compare(compareBound.getBottom(), -20) == 0)
		        && compareBound.getOrigin().equals("crossing intersecting"));
	}


	/**
	 * Test the non-passing of a Bound which does not intersect the filter area.
	 */
	@Test
	public final void testProcessBoundContainer3() {
		polyAreaFilter.process(new BoundContainer(nonIntersectingBound));
		polyAreaFilter.complete();
		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test a Node that falls inside the filter area.
	 */
	@Test
	public final void testIsNodeWithinArea1() {
		assertTrue(
		        "Node lying inside filter area not considered inside area.",
		        polyAreaFilter.isNodeWithinArea(inAreaNode));
	}


	/**
	 * Test a Node that falls outside the filter area (inside the notched-out area of the polygon).
	 */
	@Test
	public final void testIsNodeWithinArea2() {
		assertFalse(
		        "Node lying outside filter area not considered outside area.",
		        polyAreaFilter.isNodeWithinArea(outOfAreaNode));
	}


	/**
	 * Test a Node that falls on the edge of the filter area.
	 */
	@Test
	public final void testIsNodeWithinArea3() {
		assertFalse(
		        "Node lying on edge of filter area not considered inside area.",
		        polyAreaFilter.isNodeWithinArea(edgeNode));
	}
}
