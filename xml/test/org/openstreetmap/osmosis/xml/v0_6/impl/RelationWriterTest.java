// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Tests the XML relation writer implementation.
 */
public class RelationWriterTest {

	private StringWriter testWriter;
	private BufferedWriter testBufferedWriter;
	private RelationWriter testRelationWriter;
	private Date timestamp;
	// If any tests fail, it could be because the regexes have broken. There are a number of
	// variations which are valid XML which the regexes won't catch. They might need any number of
	// \\s* to account for variable whitespace, or the order of attributes may have shifted.
	private final String relationOpeningMatch = "^\\s*<relation\\s*"
			+ "id=['\"]1234['\"]\\s*"
	        + "version=['\"]2['\"]\\s*"
			+ "timestamp=['\"]2013-10-07T10:24:31Z?['\"]\\s*"
	        + "uid=['\"]23['\"]\\s*"
	        + "user=['\"]someuser['\"]\\s*"
	        + ">\\s*";
	private final String nodeMemberMatch = "\\s*<member\\s*"
			+ "type=['\"]node['\"]\\s*"
			+ "ref=['\"]2345['\"]\\s*"
			+ "role=['\"]noderole['\"]\\s*/>\\s*";
	private final String wayMemberMatch = "\\s*<member\\s*"
			+ "type=['\"]way['\"]\\s*"
			+ "ref=['\"]3456['\"]\\s*"
			+ "role=['\"]wayrole['\"]\\s*/>\\s*";
	private final String relationMemberMatch = "\\s*<member\\s*"
			+ "type=['\"]relation['\"]\\s*"
			+ "ref=['\"]4567['\"]\\s*"
			+ "role=['\"]relationrole['\"]\\s*/>\\s*";
	private final String relationTagMatch = "\\s*<tag\\s*"
			+ "k=['\"]relationkey['\"]\\s*"
			+ "v=['\"]relationvalue['\"]\\s*/>\\s*";
	private final String relationClosingMatch = "\\s*</relation>\\s*$";


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		testWriter = new StringWriter();
		testBufferedWriter = new BufferedWriter(testWriter);
		testRelationWriter = new RelationWriter("relation", 2);
		testRelationWriter.setWriter(testBufferedWriter);
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
	 * Test writing out a normal Relation element. 
	 */
	@Test
	public final void testProcessNormalRelation() {
		Relation relation =
			new Relation(new CommonEntityData(1234, 2, timestamp, new OsmUser(23, "someuser"), 0));
		relation.getMembers().add(new RelationMember(2345, EntityType.Node, "noderole"));
		relation.getMembers().add(new RelationMember(3456, EntityType.Way, "wayrole"));
		relation.getMembers().add(new RelationMember(4567, EntityType.Relation, "relationrole"));
		relation.getTags().add(new Tag("relationkey", "relationvalue"));
		
		testRelationWriter.process(relation);
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String [] strArray = testWriter.toString().split("\\n", 6);
		assertTrue("Relation opening element does not match.", strArray[0].matches(relationOpeningMatch));
		assertTrue("Relation member node does not match.", strArray[1].matches(nodeMemberMatch));
		assertTrue("Relation member way does not match.", strArray[2].matches(wayMemberMatch));
		assertTrue("Relation member relation does not match.", strArray[3].matches(relationMemberMatch));
		assertTrue("Relation tag does not match.", strArray[4].matches(relationTagMatch));
		assertTrue("Relation closing element does not match.", strArray[5].matches(relationClosingMatch));
	}

	
	/**
	 * Test writing of a Relation element with no user. 
	 */
	@Test
	public final void testProcessRelationWithNoUser() {
		Relation relation =
			new Relation(new CommonEntityData(1234, 2, timestamp, OsmUser.NONE, 0));
		relation.getMembers().add(new RelationMember(2345, EntityType.Node, "noderole"));
		relation.getMembers().add(new RelationMember(3456, EntityType.Way, "wayrole"));
		relation.getMembers().add(new RelationMember(4567, EntityType.Relation, "relationrole"));
		relation.getTags().add(new Tag("relationkey", "relationvalue"));
		
		testRelationWriter.process(relation);
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String relationOpeningNoUserMatch = "^\\s*<relation\\s*"
				+ "id=['\"]1234['\"]\\s*"
				+ "version=['\"]2['\"]\\s*"
				+ "timestamp=['\"]2013-10-07T10:24:31Z?['\"]\\s*"
				+ ">\\s*";
		String [] strArray = testWriter.toString().split("\\n", 6);
		assertTrue(strArray[0].matches(relationOpeningNoUserMatch));
		assertTrue(strArray[1].matches(nodeMemberMatch));
		assertTrue(strArray[2].matches(wayMemberMatch));
		assertTrue(strArray[3].matches(relationMemberMatch));
		assertTrue(strArray[4].matches(relationTagMatch));
		assertTrue(strArray[5].matches(relationClosingMatch));
	}
	
	
	/**
	 * Test writing out a Relation element with no tags. 
	 */
	@Test
	public final void testProcessRelationNoTags() {
		Relation relation =
			new Relation(new CommonEntityData(1234, 2, timestamp, new OsmUser(23, "someuser"), 0));
		relation.getMembers().add(new RelationMember(2345, EntityType.Node, "noderole"));
		relation.getMembers().add(new RelationMember(3456, EntityType.Way, "wayrole"));
		relation.getMembers().add(new RelationMember(4567, EntityType.Relation, "relationrole"));
		
		testRelationWriter.process(relation);
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		String [] strArray = testWriter.toString().split("\\n", 5);
		assertTrue(strArray[0].matches(relationOpeningMatch));
		assertTrue(strArray[1].matches(nodeMemberMatch));
		assertTrue(strArray[2].matches(wayMemberMatch));
		assertTrue(strArray[3].matches(relationMemberMatch));
		assertTrue(strArray[4].matches(relationClosingMatch));
	}
}
