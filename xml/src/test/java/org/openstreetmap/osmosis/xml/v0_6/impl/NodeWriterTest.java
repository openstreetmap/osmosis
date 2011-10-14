// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Tests the XML node writer implementation.
 */
public class NodeWriterTest {

	private StringWriter testWriter;
	private BufferedWriter testBufferedWriter;
	private NodeWriter testNodeWriter;
	private Date timestamp;
	// If any tests fail, it could be because the regexes have broken. There are a number of
	// variations which are valid XML which the regexes won't catch. They might need any number of
	// \\s* to account for variable whitespace, or the order of attributes may have shifted.
	private final String nodeOpeningMatch = "^\\s*<node\\s*"
		+ "id=['\"]1234['\"]\\s*"
        + "version=['\"]2['\"]\\s*"
		+ "timestamp=['\"]2013-10-07T10:24:31Z?['\"]\\s*"
        + "uid=['\"]23['\"]\\s*"
        + "user=['\"]someuser['\"]\\s*"
        + "lat=['\"]20.1234568['\"]\\s*"
        + "lon=['\"]-21.9876543['\"]\\s*"
        + ">\\s*";
	private final String nodeTagMatch = "\\s*<tag\\s*"
			+ "k=['\"]nodekey['\"]\\s*"
			+ "v=['\"]nodevalue['\"]\\s*/>\\s*";
	private final String nodeClosingMatch = "\\s*</node>\\s*$";
	

	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		testWriter = new StringWriter();
		testBufferedWriter = new BufferedWriter(testWriter);
		testNodeWriter = new NodeWriter("node", 2);
		testNodeWriter.setWriter(testBufferedWriter);
		Calendar calendar;
		calendar = Calendar.getInstance();
		calendar.set(Calendar.ZONE_OFFSET, 0);
		calendar.set(Calendar.DST_OFFSET, 0);
		calendar.set(Calendar.YEAR, 2013);
		calendar.set(Calendar.MONTH, Calendar.OCTOBER);
		calendar.set(Calendar.DAY_OF_MONTH, 7);
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 24);
		calendar.set(Calendar.SECOND, 31);
		calendar.set(Calendar.MILLISECOND, 0);
		timestamp = calendar.getTime();
	}


	/**
	 * Performs post-test activities.
	 * 
	 * @throws IOException
	 *             if stream cleanup fails.
	 */
	@After
	public void tearDown() throws IOException {
		testBufferedWriter.close();
		testWriter.close();
	}


	/**
	 * Test writing out a normal Node element. 
	 */
	@Test
	public final void testProcessNormalNode() {
		Node node =
			new Node(
				new CommonEntityData(1234, 2, timestamp, new OsmUser(23, "someuser"), 0),
				20.12345678, -21.98765432);
		node.getTags().add(new Tag("nodekey", "nodevalue"));
		testNodeWriter.process(node);
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String [] strArray = testWriter.toString().split("\\n", 3);
		assertTrue("Node opening element does not match.", strArray[0].matches(nodeOpeningMatch));
		assertTrue("Node tag does not match.", strArray[1].matches(nodeTagMatch));
		assertTrue("Node closing element does not match.", strArray[2].matches(nodeClosingMatch));
	}

	
	/**
	 * Test writing out a Node element with no tags. 
	 */
	@Test
	public final void testProcessNodeNoTags() {
		testNodeWriter.process(
				new Node(
					new CommonEntityData(
						1234, 2, timestamp,
						new OsmUser(23, "someuser"), 0,
						new ArrayList<Tag>()),
					20.12345678,
					-21.98765432));
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String regexMatch = "^\\s*<node\\s*"
				+ "id=['\"]1234['\"]\\s*"
		        + "version=['\"]2['\"]\\s*"
				+ "timestamp=['\"]2013-10-07T10:24:31Z?['\"]\\s*"
		        + "uid=['\"]23['\"]\\s*"
		        + "user=['\"]someuser['\"]\\s*"
		        + "lat=['\"]20.1234568['\"]\\s*"
		        + "lon=['\"]-21.9876543['\"]\\s*"
		        + "/>\\s*$";
		assertTrue(testWriter.toString().matches(regexMatch));
	}

	
	/**
	 * Test writing of a Node element with no user. 
	 */
	@Test
	public final void testProcessNodeWithNoUser() {
		Node node = new Node(new CommonEntityData(1234, 2, timestamp, OsmUser.NONE, 0), 20.12345678, -21.98765432);
		node.getTags().add(new Tag("nodekey", "nodevalue"));
		testNodeWriter.process(node);
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String nodeOpeningNoUserMatch = "^\\s*<node\\s*"
			+ "id=['\"]1234['\"]\\s*"
	        + "version=['\"]2['\"]\\s*"
			+ "timestamp=['\"]2013-10-07T10:24:31Z?['\"]\\s*"
	        + "lat=['\"]20.1234568['\"]\\s*"
	        + "lon=['\"]-21.9876543['\"]\\s*"
	        + ">\\s*";
		String [] strArray = testWriter.toString().split("\\n", 3);
		assertTrue("Node opening element does not match.", strArray[0].matches(nodeOpeningNoUserMatch));
		assertTrue("Node tag does not match.", strArray[1].matches(nodeTagMatch));
		assertTrue("Node closing element does not match.", strArray[2].matches(nodeClosingMatch));
	}
}
