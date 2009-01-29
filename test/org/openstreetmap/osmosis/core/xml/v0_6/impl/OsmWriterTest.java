// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
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
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class OsmWriterTest {

	private StringWriter testWriter;
	private BufferedWriter testBufferedWriter;
	private OsmWriter testOsmWriter;


	@Before
	public void setUp() throws Exception {
		testWriter = new StringWriter();
		testBufferedWriter = new BufferedWriter(testWriter);
		testOsmWriter = new OsmWriter("osm", 0, true);
		testOsmWriter.setWriter(testBufferedWriter);
	}


	@After
	public void tearDown() throws Exception {
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
	@Test(expected=OsmosisRuntimeException.class)
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
								1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), new ArrayList<Tag>(), 20, 20)));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Node.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess4() {
		testOsmWriter.process(
				new NodeContainer(
						new Node(1234, 0, new Date(), new OsmUser(12, "OsmosisTest"), new ArrayList<Tag>(), 20, 20)));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
		fail("Expected to throw an exception.");
	}


	/**
	 * Test processing a Way.
	 */
	@Test
	public final void testProcess6() {
		Way testWay;
		
		testWay =
			new WayBuilder(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"))
			.addWayNode(new WayNode(1234))
			.addWayNode(new WayNode(1235))
			.addTag(new Tag("test_key1", "test_value1"))
			.buildEntity();
		
		testOsmWriter.process(new WayContainer(testWay));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Way.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess7() {
		Way testWay;
		
		testWay =
			new WayBuilder(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"))
			.addWayNode(new WayNode(1234))
			.addWayNode(new WayNode(1235))
			.addTag(new Tag("test_key1", "test_value1"))
			.buildEntity();
		
		testOsmWriter.process(new WayContainer(testWay));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}


	/**
	 * Test processing a Relation.
	 */
	@Test
	public final void testProcess8() {
		Relation testRelation;
		
		testRelation =
			new RelationBuilder(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"))
			.addMember(new RelationMember(1234, EntityType.Node, "role1"))
			.addTag(new Tag("test_key1", "test_value1"))
			.buildEntity();
		
		testOsmWriter.process(new RelationContainer(testRelation));
		// Nothing to assert; just expect no exception
	}

	
	/**
	 * Test processing a Bound after a Relation.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess9() {
		Relation testRelation;
		
		testRelation =
			new RelationBuilder(3456, 0, new Date(), new OsmUser(12, "OsmosisTest"))
			.addMember(new RelationMember(1234, EntityType.Node, "role1"))
			.addTag(new Tag("test_key1", "test_value1"))
			.buildEntity();
		testOsmWriter.process(new RelationContainer(testRelation));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}
}
