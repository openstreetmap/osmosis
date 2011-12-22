// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.testutil.v0_6.SinkEntityInspector;

/**
 * Tests the TagFilter implementation.
 * 
 * @author Andrew Byrd
 * Based on tests written by Karl Newman
 */
public class TagFilterTest {

	private SinkEntityInspector entityInspector;
	private TagFilter tagFilter;

	private Node amenityNode;
	private NodeContainer amenityNodeContainer;

	private Node taglessNode;
	private NodeContainer taglessNodeContainer;

	private Way motorwayWay;
	private WayContainer motorwayWayContainer;

	private Way residentialWay;
	private WayContainer residentialWayContainer;

	private Relation testRelation;
	private RelationContainer testRelationContainer;

	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		OsmUser user;
		List<Tag> tags;
		
		user = new OsmUser(12, "OsmosisTest");
			
		tags = Arrays.asList(new Tag("amenity", "bank"), new Tag("Akey", "Avalue"));
		amenityNode = new Node(new CommonEntityData(1101, 0, new Date(), user, 0, tags), 1, 2);
		amenityNodeContainer = new NodeContainer(amenityNode);

		tags = new ArrayList<Tag>();
		taglessNode = new Node(new CommonEntityData(1102, 0, new Date(), user, 0, tags), 3, 4);
		taglessNodeContainer = new NodeContainer(taglessNode);

		tags = Arrays.asList(new Tag("highway", "motorway"), new Tag("Bkey", "Bvalue"));
		motorwayWay = new Way(new CommonEntityData(2201, 0, new Date(), user, 0, tags), new ArrayList<WayNode>());
		motorwayWayContainer = new WayContainer(motorwayWay);

		tags = Arrays.asList(new Tag("highway", "residential"), new Tag("Ckey", "Cvalue"));
		residentialWay =
			new Way(new CommonEntityData(2202, 0, new Date(), user, 0, tags), new ArrayList<WayNode>());
		residentialWayContainer = new WayContainer(residentialWay);

		tags = Arrays.asList(new Tag("Dkey", "Dvalue"));
		testRelation =
			new Relation(new CommonEntityData(3301, 0, new Date(), user, 0, tags), new ArrayList<RelationMember>());
		testRelationContainer = new RelationContainer(testRelation);
	}


	/**
	 * Performs post-test activities.
	 */
	@After
	public void tearDown() {
		// nothing to do here.
	}


	/**
	 * Test passing a node which matches the filter.
	 */
	@Test
	public final void testAcceptNode() {
		Set<String> keys = new HashSet<String>(Arrays.asList("amenity"));
		Map<String, Set<String>> keyValues = new HashMap<String, Set<String>>();
		keyValues.put("key", new HashSet<String>(Arrays.asList("valone", "valtwo")));
		tagFilter = new TagFilter("accept-nodes", keys, keyValues);
		entityInspector = new SinkEntityInspector();
		tagFilter.setSink(entityInspector);

		tagFilter.process(amenityNodeContainer);
		tagFilter.process(taglessNodeContainer);
		tagFilter.process(residentialWayContainer);
		tagFilter.complete();

		List<EntityContainer> expectedResult = Arrays.asList(amenityNodeContainer, residentialWayContainer);
		assertTrue(entityInspector.getProcessedEntities().equals(expectedResult));
		tagFilter.release();
	}


	/**
	 * Test rejecting a way which matches the filter.
	 */
	@Test
	public final void testRejectWay() {
		Set<String> keys = new HashSet<String>();
		Map<String, Set<String>> keyValues = new HashMap<String, Set<String>>();
		keyValues.put("highway", new HashSet<String>(Arrays.asList("motorway", "motorway_link")));
		tagFilter = new TagFilter("reject-ways", keys, keyValues);
		entityInspector = new SinkEntityInspector();
		tagFilter.setSink(entityInspector);

		tagFilter.process(amenityNodeContainer);
		tagFilter.process(residentialWayContainer);
		tagFilter.process(motorwayWayContainer);
		tagFilter.complete();

		List<EntityContainer> expectedResult = Arrays.asList(amenityNodeContainer, residentialWayContainer);
		assertTrue(entityInspector.getProcessedEntities().equals(expectedResult));
		tagFilter.release();
	}


	/**
	 * Test rejecting a relation without tag-based filtering.
	 */
	@Test
	public final void testRejectRelation() {
		Set<String> keys = new HashSet<String>();
		Map<String, Set<String>> keyValues = new HashMap<String, Set<String>>();
		tagFilter = new TagFilter("reject-relations", keys, keyValues);
		entityInspector = new SinkEntityInspector();
		tagFilter.setSink(entityInspector);

		tagFilter.process(amenityNodeContainer);
		tagFilter.process(residentialWayContainer);
		tagFilter.process(testRelationContainer);
		tagFilter.complete();

		List<EntityContainer> expectedResult = Arrays.asList(amenityNodeContainer, residentialWayContainer);
		assertTrue(entityInspector.getProcessedEntities().equals(expectedResult));
		tagFilter.release();
	}

}
