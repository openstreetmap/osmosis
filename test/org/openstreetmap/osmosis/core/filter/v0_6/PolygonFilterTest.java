// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.v0_6;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.test.task.v0_6.SinkEntityInspector;

/**
 * Tests the polygon area filter implementation.
 * 
 * @author Karl Newman
 */
public class PolygonFilterTest {

	private File polygonFile;
	private SinkEntityInspector entityInspector;
	private AreaFilter polyAreaFilter;
	private Bound intersectingBound;
	private Bound crossingIntersectingBound;
	private Bound nonIntersectingBound;
	private Node inAreaNode;
	private Node outOfAreaNode;
	private Node edgeNode;


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		List<Tag> tags;
		
		// All nodes have an empty tags list.
		tags = new ArrayList<Tag>();
		
		polygonFile = new File("test/org/openstreetmap/osmosis/core/filter/v0_6/testPolygon.txt");
		entityInspector = new SinkEntityInspector();
		// polyAreaFilter has a notch out of the Northeast corner.
		polyAreaFilter = new PolygonFilter(IdTrackerType.IdList, polygonFile, false, false, false);
		polyAreaFilter.setSink(entityInspector);
		intersectingBound = new Bound(30, 0, 30, 0, "intersecting");
		crossingIntersectingBound = new Bound(-10, 10, 30, -30, "crossing intersecting");
		nonIntersectingBound = new Bound(30, 15, 30, 15, "nonintersecting");
		inAreaNode = new Node(1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0, tags, 5, 10);
		outOfAreaNode = new Node(1235, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0, tags, 15, 15);
		edgeNode = new Node(1236, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0, tags, 15, 10);
	}


	/**
	 * Performs post-test activities.
	 */
	@After
	public void tearDown() {
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
