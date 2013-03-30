// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Tests the XML osm element writer implementation.
 */
public class OsmWriterTest {

	private StringWriter testWriter;
	private BufferedWriter testBufferedWriter;
	private OsmWriter testOsmWriter;


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		testWriter = new StringWriter();
		testBufferedWriter = new BufferedWriter(testWriter);
		testOsmWriter = new OsmWriter("osm", 0, true, true);
		testOsmWriter.setWriter(testBufferedWriter);
	}


	/**
	 * Performs post-test activities.
	 * 
	 * @throws IOException
	 *             if IO stream cleanup fails.
	 */
	@After
	public void tearDown() throws IOException {
		testBufferedWriter.close();
		testWriter.close();
		testOsmWriter = null;
	}


	/**
	 * Test processing a single Bound entity.
	 */
	@Test
	public final void testProcess1() {
		testOsmWriter.process(new BoundContainer(new Bound("source")));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a repeated Bound entity.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testProcess2() {
		testOsmWriter.process(new BoundContainer(new Bound("source")));
		testOsmWriter.process(new BoundContainer(new Bound("source2")));
		fail("Expected to throw an exception.");
	}


	/**
	 * Test processing a Node entity.
	 */
	@Test
	public final void testProcess3() {
		testOsmWriter.process(
				new NodeContainer(
					new Node(
						new CommonEntityData(
								1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0, new ArrayList<Tag>()),
						20, 20)));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Node.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testProcess4() {
		testOsmWriter.process(new NodeContainer(
				new Node(
						new CommonEntityData(1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0,
								new ArrayList<Tag>()),
						20, 20)));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
		fail("Expected to throw an exception.");
	}


	/**
	 * Test processing a Way.
	 */
	@Test
	public final void testProcess6() {
		Way testWay;
		
		testWay = new Way(new CommonEntityData(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0));
		testWay.getWayNodes().add(new WayNode(1234));
		testWay.getWayNodes().add(new WayNode(1235));
		testWay.getTags().add(new Tag("test_key1", "test_value1"));
		
		testOsmWriter.process(new WayContainer(testWay));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Way.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testProcess7() {
		Way testWay;
		
		testWay = new Way(new CommonEntityData(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0));
		testWay.getWayNodes().add(new WayNode(1234));
		testWay.getWayNodes().add(new WayNode(1235));
		testWay.getTags().add(new Tag("test_key1", "test_value1"));
		
		testOsmWriter.process(new WayContainer(testWay));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}


	/**
	 * Test processing a Relation.
	 */
	@Test
	public final void testProcess8() {
		Relation testRelation;
		
		testRelation = new Relation(new CommonEntityData(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0));
		testRelation.getMembers().add(new RelationMember(1234, EntityType.Node, "role1"));
		testRelation.getTags().add(new Tag("test_key1", "test_value1"));
		
		testOsmWriter.process(new RelationContainer(testRelation));
		// Nothing to assert; just expect no exception
	}

	
	/**
	 * Test processing a Bound after a Relation.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testProcess9() {
		Relation testRelation;
		
		testRelation = new Relation(new CommonEntityData(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"), 0));
		testRelation.getMembers().add(new RelationMember(1234, EntityType.Node, "role1"));
		testRelation.getTags().add(new Tag("test_key1", "test_value1"));
		
		testOsmWriter.process(new RelationContainer(testRelation));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}
}
