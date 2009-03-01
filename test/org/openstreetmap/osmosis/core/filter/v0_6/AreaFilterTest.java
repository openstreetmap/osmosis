// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.v0_6;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.test.task.v0_6.SinkEntityInspector;


/**
 * Tests the area filter implementation.
 */
public class AreaFilterTest {

	private static final String TEST_USER = "OsmosisTest";
	private static final int TEST_USER_ID = 32;
	private SinkEntityInspector entityInspector;
	private AreaFilter simpleAreaFilter;
	private AreaFilter simpleCompleteWayFilter;
	private AreaFilter simpleCompleteRelationFilter;
	private AreaFilter simpleCompleteWayRelationFilter;
	private Node inAreaNode;
	private Node outOfAreaNode;
	private Node inAreaWayNode1;
	private Node inAreaWayNode2;
	private Node outOfAreaWayNode1;
	private Node outOfAreaWayNode2;
	private Way inAreaWay;
	private Way outOfAreaWay;
	private Way inOutWay;
	private Way mangledInOutWay1;
	private Relation inAreaRelation;
	private Relation outOfAreaRelation1;
	private Relation inOutRelation1;
	private Relation inOutRelation2;
	private Relation mangledInOutRelation1;
	private Relation mangledInOutRelation2;
	private Relation mangledCompleteInOutRelation1;


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		setUpFilters();
		setUpNodes();
		setUpWays();
		setUpRelations();
	}


	private void setUpFilters() {
		entityInspector = new SinkEntityInspector();
		// simpleAreaFilter doesn't cross antimeridian; no complete ways or relations
		simpleAreaFilter = new BoundingBoxFilter(
		        IdTrackerType.IdList,
		        -20,
		        20,
		        20,
		        -20,
		        true,
		        false,
		        false);
		// simpleCompleteWayFilter doesn't cross antimeridian; complete ways but not relations
		simpleCompleteWayFilter = new BoundingBoxFilter(
		        IdTrackerType.IdList,
		        -20,
		        20,
		        20,
		        -20,
		        true,
		        true,
		        false);
		// simpleCompleteRelationFilter doesn't cross antimeridian; complete ways but not relations
		simpleCompleteRelationFilter = new BoundingBoxFilter(
		        IdTrackerType.IdList,
		        -20,
		        20,
		        20,
		        -20,
		        true,
		        false,
		        true);
		// simpleCompleteWayRelationFilter doesn't cross antimeridian; complete ways and relations
		simpleCompleteWayRelationFilter = new BoundingBoxFilter(
		        IdTrackerType.IdList,
		        -20,
		        20,
		        20,
		        -20,
		        true,
		        true,
		        true);
		simpleAreaFilter.setSink(entityInspector);
		simpleCompleteWayFilter.setSink(entityInspector);
		simpleCompleteRelationFilter.setSink(entityInspector);
		simpleCompleteWayRelationFilter.setSink(entityInspector);
	}


	private void setUpNodes() {
		inAreaNode = new Node(1234, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), 10, 10);
		inAreaNode.getTags().add(new Tag("test_key1", "test_value1"));
		
		outOfAreaNode = new Node(1235, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), 30, 30);
		
		inAreaWayNode1 = new Node(2345, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), 10, 10);
		inAreaWayNode2 = new Node(2346, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), -10, -10);
		
		outOfAreaWayNode1 = new Node(2347, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), -30, -30);
		outOfAreaWayNode2 = new Node(2348, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER), -40, -40);
	}


	private void setUpWays() {
		inAreaWay = new Way(3456, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		inAreaWay.getWayNodes().add(new WayNode(inAreaWayNode1.getId()));
		inAreaWay.getTags().add(new Tag("test_key2", "test_value2"));
		
		outOfAreaWay = new Way(3457, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		outOfAreaWay.getWayNodes().addAll(
				Arrays.asList(new WayNode[] {
						new WayNode(outOfAreaWayNode1.getId()),
						new WayNode(outOfAreaWayNode2.getId())
				})
		);
		
		inOutWay = new Way(3458, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		inOutWay.getWayNodes().addAll(
				Arrays.asList(new WayNode[] {
						new WayNode(inAreaWayNode1.getId()),
						new WayNode(outOfAreaWayNode1.getId()),
						new WayNode(inAreaWayNode2.getId()),
						new WayNode(outOfAreaWayNode2.getId())
				})
		);
		inOutWay.getTags().add(new Tag("test_key3", "test_value3"));
		
		// mangledInOutWay1 is mangled by completeWays=false
		mangledInOutWay1 = new Way(inOutWay.getId(), 0, inOutWay.getTimestamp(), inOutWay.getUser());
		mangledInOutWay1.getWayNodes().addAll(
				Arrays.asList(new WayNode[] {
						new WayNode(inAreaWayNode1.getId()),
						new WayNode(inAreaWayNode2.getId())
				})
		);
		mangledInOutWay1.getTags().addAll(inOutWay.getTags());
	}


	private void setUpRelations() {
		inAreaRelation = new Relation(4567, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		inAreaRelation.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode1.getId(), EntityType.Node, "node1"),
						new RelationMember(inAreaWayNode2.getId(), EntityType.Node, "node2"),
						new RelationMember(inAreaWay.getId(), EntityType.Way, "way1")
				})
		);
		inAreaRelation.getTags().add(new Tag("test_key4", "test_value4"));
		
		outOfAreaRelation1 = new Relation(4568, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		outOfAreaRelation1.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(outOfAreaWayNode1.getId(), EntityType.Node, "node1"),
						new RelationMember(outOfAreaWayNode2.getId(), EntityType.Node, "node2"),
						new RelationMember(outOfAreaWay.getId(), EntityType.Way, "way1")
				})
		);
		
		inOutRelation2 = new Relation(4570, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		inOutRelation2.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode2.getId(), EntityType.Node, "node1"),
						new RelationMember(outOfAreaWayNode2.getId(), EntityType.Node, "node2"),
						new RelationMember(inOutWay.getId(), EntityType.Way, "way1")
				})
		);
		inOutRelation2.getTags().add(new Tag("test_key5", "test_value5"));
		
		inOutRelation1 = new Relation(4569, 0, new Date(), new OsmUser(TEST_USER_ID, TEST_USER));
		inOutRelation1.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode1.getId(), EntityType.Node, "node1"),
						new RelationMember(outOfAreaWayNode1.getId(), EntityType.Node, "node2"),
						new RelationMember(inOutWay.getId(), EntityType.Way, "way1"),
						new RelationMember(inOutRelation2.getId(), EntityType.Relation, "relation1")
				})
		);
		inOutRelation1.getTags().add(new Tag("test_key4", "test_value4"));
		
		mangledInOutRelation1 = new Relation(inOutRelation1.getId(),
				inOutRelation1.getVersion(),
				inOutRelation1.getTimestamp(),
				inOutRelation1.getUser());
		mangledInOutRelation1.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode1.getId(), EntityType.Node, "node1"),
						new RelationMember(inOutWay.getId(), EntityType.Way, "way1")
				})
		);
		mangledInOutRelation1.getTags().addAll(inOutRelation1.getTags());
		
		mangledInOutRelation2 =  new Relation(
				inOutRelation2.getId(),
				inOutRelation2.getVersion(),
				inOutRelation2.getTimestamp(),
				inOutRelation2.getUser());
		mangledInOutRelation2.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode2.getId(), EntityType.Node, "node1"),
						new RelationMember(inOutWay.getId(), EntityType.Way, "way1")
				})
		);
		mangledInOutRelation2.getTags().addAll(inOutRelation2.getTags());
		
		mangledCompleteInOutRelation1 = new Relation(
				inOutRelation1.getId(),
				inOutRelation1.getVersion(),
				inOutRelation1.getTimestamp(),
				inOutRelation1.getUser());
		mangledCompleteInOutRelation1.getMembers().addAll(
				Arrays.asList(new RelationMember[] {
						new RelationMember(inAreaWayNode1.getId(), EntityType.Node, "node1"),
						new RelationMember(inOutWay.getId(), EntityType.Way, "way1"),
						new RelationMember(inOutRelation2.getId(), EntityType.Relation, "relation1")
				})
		);
		mangledCompleteInOutRelation1.getTags().addAll(inOutRelation1.getTags());
	}


	/**
	 * Performs post-test activities.
	 */
	@After
	public void tearDown() {
		simpleAreaFilter.release();
		simpleCompleteWayFilter.release();
		simpleCompleteRelationFilter.release();
		simpleCompleteWayRelationFilter.release();
	}


	/**
	 * Test simple passing of a node that falls within the area.
	 */
	@Test
	public final void testProcessNodeContainer1() {
		Entity compareNode;

		simpleAreaFilter.process(new NodeContainer(inAreaNode));
		simpleAreaFilter.complete();

		compareNode = entityInspector.getLastEntityContainer().getEntity();
		assertTrue(compareNode instanceof Node && inAreaNode.compareTo((Node) compareNode) == 0);
	}


	/**
	 * Test simple non-passing of node that falls outside the area.
	 */
	@Test
	public final void testProcessNodeContainer2() {
		simpleAreaFilter.process(new NodeContainer(outOfAreaNode));
		simpleAreaFilter.complete();
		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test passing of nodes and ways strictly inside the area with completeWays = false.
	 */
	@Test
	public final void testProcessWayContainer1() {
		Iterator<EntityContainer> ecIterator;

		simpleAreaFilter.process(new NodeContainer(inAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(inAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(inAreaWay));
		simpleAreaFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test non-passing of nodes and ways which are strictly outside the area with completeWays =
	 * false.
	 */
	@Test
	public final void testProcessWayContainer2() {
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(outOfAreaWay));
		simpleAreaFilter.complete();

		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test mangling of a way which has nodes both inside and outside the area with completeWays =
	 * false.
	 */
	@Test
	public final void testProcessWayContainer3() {
		Iterator<EntityContainer> ecIterator;

		simpleAreaFilter.process(new NodeContainer(inAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(inAreaWayNode2));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(inOutWay));
		simpleAreaFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && mangledInOutWay1.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of nodes and ways strictly inside the area with completeWays = true.
	 */
	@Test
	public final void testProcessWayContainer4() {
		Iterator<EntityContainer> ecIterator;

		simpleCompleteWayFilter.process(new NodeContainer(inAreaWayNode1));
		simpleCompleteWayFilter.process(new NodeContainer(inAreaWayNode2));
		simpleCompleteWayFilter.process(new WayContainer(inAreaWay));
		simpleCompleteWayFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test non-passing of nodes and ways which are strictly outside the area with completeWays =
	 * true.
	 */
	@Test
	public final void testProcessWayContainer5() {
		simpleCompleteWayFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleCompleteWayFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleCompleteWayFilter.process(new WayContainer(outOfAreaWay));
		simpleCompleteWayFilter.complete();

		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test passing of a way which has nodes both inside and outside the area with completeWays =
	 * true.
	 */
	@Test
	public final void testProcessWayContainer6() {
		Iterator<EntityContainer> ecIterator;

		simpleCompleteWayFilter.process(new NodeContainer(inAreaWayNode1));
		simpleCompleteWayFilter.process(new NodeContainer(inAreaWayNode2));
		simpleCompleteWayFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleCompleteWayFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleCompleteWayFilter.process(new WayContainer(inOutWay));
		simpleCompleteWayFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0);
		assertTrue(outOfAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0);
		assertTrue(outOfAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0);
		assertTrue(inOutWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of a relation where all referenced members are strictly inside the filter area
	 * (and completeRelations = false).
	 */
	@Test
	public final void testProcessRelationContainer1() {
		Iterator<EntityContainer> ecIterator;

		simpleAreaFilter.process(new NodeContainer(inAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(inAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(inAreaWay));
		simpleAreaFilter.process(new RelationContainer(inAreaRelation));
		simpleAreaFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && inAreaRelation.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of a relation where all referenced members are strictly outside the filter area
	 * (and completeRelations = false).
	 */
	@Test
	public final void testProcessRelationContainer2() {
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(outOfAreaWay));
		simpleAreaFilter.process(new RelationContainer(outOfAreaRelation1));
		simpleAreaFilter.complete();

		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test passing of a relation where referenced members are both inside and outside the filter
	 * area (and completeRelations = false).
	 */
	@Test
	public final void testProcessRelationContainer3() {
		Iterator<EntityContainer> ecIterator;

		simpleAreaFilter.process(new NodeContainer(inAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(inAreaWayNode2));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleAreaFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleAreaFilter.process(new WayContainer(inOutWay));
		simpleAreaFilter.process(new RelationContainer(inOutRelation1));
		simpleAreaFilter.process(new RelationContainer(inOutRelation2));
		simpleAreaFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && mangledInOutWay1.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && mangledInOutRelation1.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && mangledInOutRelation2.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of a relation where all referenced members are strictly inside the filter area
	 * (and completeRelations = true).
	 */
	@Test
	public final void testProcessRelationContainer4() {
		Iterator<EntityContainer> ecIterator;

		simpleCompleteRelationFilter.process(new NodeContainer(inAreaWayNode1));
		simpleCompleteRelationFilter.process(new NodeContainer(inAreaWayNode2));
		simpleCompleteRelationFilter.process(new WayContainer(inAreaWay));
		simpleCompleteRelationFilter.process(new RelationContainer(inAreaRelation));
		simpleCompleteRelationFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && inAreaRelation.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of a relation where all referenced members are strictly outside the filter area
	 * (and completeRelations = true).
	 */
	@Test
	public final void testProcessRelationContainer5() {
		simpleCompleteRelationFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleCompleteRelationFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleCompleteRelationFilter.process(new WayContainer(outOfAreaWay));
		simpleCompleteRelationFilter.process(new RelationContainer(outOfAreaRelation1));
		simpleCompleteRelationFilter.complete();

		assertNull(entityInspector.getLastEntityContainer());
	}


	/**
	 * Test passing of a relation where referenced members are both inside and outside the filter
	 * area (and completeRelations = false).
	 */
	@Test
	public final void testProcessRelationContainer6() {
		Iterator<EntityContainer> ecIterator;

		simpleCompleteRelationFilter.process(new NodeContainer(inAreaWayNode1));
		simpleCompleteRelationFilter.process(new NodeContainer(inAreaWayNode2));
		simpleCompleteRelationFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleCompleteRelationFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleCompleteRelationFilter.process(new WayContainer(inOutWay));
		simpleCompleteRelationFilter.process(new RelationContainer(inOutRelation1));
		simpleCompleteRelationFilter.process(new RelationContainer(inOutRelation2));
		simpleCompleteRelationFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && mangledInOutWay1.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && mangledCompleteInOutRelation1.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && mangledInOutRelation2.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}


	/**
	 * Test passing of a relation where referenced members are both inside and outside the filter
	 * area (with completeWays = true and completeRelations = true).
	 */
	@Test
	public final void testProcessRelationContainer7() {
		Iterator<EntityContainer> ecIterator;

		simpleCompleteWayRelationFilter.process(new NodeContainer(inAreaWayNode1));
		simpleCompleteWayRelationFilter.process(new NodeContainer(inAreaWayNode2));
		simpleCompleteWayRelationFilter.process(new NodeContainer(outOfAreaWayNode1));
		simpleCompleteWayRelationFilter.process(new NodeContainer(outOfAreaWayNode2));
		simpleCompleteWayRelationFilter.process(new WayContainer(inOutWay));
		simpleCompleteWayRelationFilter.process(new RelationContainer(inOutRelation1));
		simpleCompleteWayRelationFilter.process(new RelationContainer(inOutRelation2));
		simpleCompleteWayRelationFilter.complete();

		ecIterator = entityInspector.getProcessedEntities().iterator();
		assertTrue(inAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && outOfAreaWayNode1.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && outOfAreaWayNode2.compareTo((Node) ecIterator.next().getEntity()) == 0
		        && inOutWay.compareTo((Way) ecIterator.next().getEntity()) == 0
		        && inOutRelation1.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && inOutRelation2.compareTo((Relation) ecIterator.next().getEntity()) == 0
		        && !ecIterator.hasNext());
	}
}
