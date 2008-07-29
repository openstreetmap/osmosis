// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import com.bretth.osmosis.core.store.DataInputStoreReader;
import com.bretth.osmosis.core.store.DataOutputStoreWriter;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;

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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StoreWriter sw = new DataOutputStoreWriter(new DataOutputStream(out));
		StoreClassRegister scr = new StoreClassRegister();
		OsmUser user1 = OsmUser.getInstance("aUser", 12);
		OsmUser user2 = OsmUser.getInstance("aUser2", 13);
		user1.store(sw, scr);
		user2.store(sw, scr);
		StoreReader sr = new DataInputStoreReader(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		OsmUser user3 = OsmUser.getInstance(sr, scr);
		OsmUser user4 = OsmUser.getInstance(sr, scr);
		assertEquals("Object not equal after retrieval from store", user1, user3);
		assertEquals("Object not equal after retrieval from store", user2, user4);
	}
}
