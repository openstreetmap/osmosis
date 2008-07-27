// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import static org.junit.Assert.*;
import org.junit.Test;

public class OsmUserTest {

	/**
	 * Ensure the equal instances are returned for the same input values.
	 */
	@Test
	public final void testGetInstance1() {
		OsmUser user1 = OsmUser.getInstance("aUser", 12);
		// create another one to make it actually do some work
		OsmUser.getInstance("bUser", 14);
		OsmUser user2 = OsmUser.getInstance("aUser", 12);
		assertEquals("Objects are not equal", user1, user2);
		assertEquals("Hash codes are not equal", user1.hashCode(), user2.hashCode());
	}


	/**
	 * Ensure different instances are returned for different input values.
	 */
	@Test
	public final void testGetInstance2() {
		OsmUser user1 = OsmUser.getInstance("aUser", 12);
		OsmUser user2 = OsmUser.getInstance("aUser", 13);
		OsmUser user3 = OsmUser.getInstance("aUser1", 12);
		assertFalse("Objects should not be equal", user1.equals(user2));
		// Note: it's not strictly a failure if the hash codes are equal, but it shouldn't happen
		assertFalse("Object hash codes should not be equal", user1.hashCode() == user2.hashCode());
		assertFalse("Objects should not be equal", user1.equals(user3));
		assertFalse("Object hash codes should not be equal", user1.hashCode() == user3.hashCode());
	}


	/**
	 * Ensure the instance is correctly written to and read from the store.
	 */
	@Test
	public final void testGetInstance3() {
		fail("Not yet implemented");
	}
}
