package com.bretth.osmosis.core.xml.v0_6.impl;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;

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
		testOsmWriter.process(new NodeContainer(new Node(1234, new Date(), new OsmUser("OsmosisTest", 12), 0, 20, 20)));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Node.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess4() {
		testOsmWriter.process(new NodeContainer(new Node(1234, new Date(), new OsmUser("OsmosisTest", 12), 0, 20, 20)));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
		fail("Expected to throw an exception.");
	}


	/**
	 * Test processing a Way.
	 */
	@Test
	public final void testProcess6() {
		Way testWay;
		testWay = new Way(3456, new Date(), new OsmUser("OsmosisTest", 12), 0);
		testWay.addWayNode(new WayNode(1234));
		testWay.addWayNode(new WayNode(1235));
		testWay.addTag(new Tag("test_key1", "test_value1"));
		testOsmWriter.process(new WayContainer(testWay));
		// Nothing to assert; just expect no exception
	}


	/**
	 * Test processing a Bound after a Way.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess7() {
		Way testWay;
		testWay = new Way(3456, new Date(), new OsmUser("OsmosisTest", 12), 0);
		testWay.addWayNode(new WayNode(1234));
		testWay.addWayNode(new WayNode(1235));
		testWay.addTag(new Tag("test_key1", "test_value1"));
		testOsmWriter.process(new WayContainer(testWay));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}


	/**
	 * Test processing a Relation.
	 */
	@Test
	public final void testProcess8() {
		Relation testRelation;
		testRelation = new Relation(3456, new Date(), new OsmUser("OsmosisTest", 12), 0);
		testRelation.addMember(new RelationMember(1234, EntityType.Node, "role1"));
		testRelation.addTag(new Tag("test_key1", "test_value1"));
		testOsmWriter.process(new RelationContainer(testRelation));
		// Nothing to assert; just expect no exception
	}

	
	/**
	 * Test processing a Bound after a Relation.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testProcess9() {
		Relation testRelation;
		testRelation = new Relation(3456, new Date(), new OsmUser("OsmosisTest", 12), 0);
		testRelation.addMember(new RelationMember(1234, EntityType.Node, "role1"));
		testRelation.addTag(new Tag("test_key1", "test_value1"));
		testOsmWriter.process(new RelationContainer(testRelation));
		testOsmWriter.process(new BoundContainer(new Bound("source")));
	}
}
