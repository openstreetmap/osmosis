// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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


/**
 * Tests the OsmUser class.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUserTest {
	
	/**
	 * Ensure that the object is created with the correct values.
	 */
	@Test
	public final void testGetInstanceValues() {
		OsmUser user;
		
		user = OsmUser.getInstance("myUserName", 1);
		
		assertEquals("The user name is incorrect.", "myUserName", user.getUserName());
		assertEquals("The user id is incorrect.", 1, user.getUserId());
	}
	
	
	/**
	 * Ensure that the object details are correct when the user name is missing.
	 */
	@Test
	public final void testGetInstanceEmptyUser() {
		OsmUser user;
		
		user = OsmUser.getInstance("", 1);
		
		assertEquals("The user name is incorrect.", "", user.getUserName());
		assertEquals("The user id is incorrect.", 1, user.getUserId());
	}
	
	
	/**
	 * Ensure the special value NO_USER is returned for equivalent input values.
	 */
	@Test
	public final void testGetInstanceNoUser() {
		OsmUser user1 = OsmUser.getInstance("", OsmUser.USER_ID_NONE);
		assertEquals("Objects are not equal", user1, OsmUser.NO_USER);
		assertEquals("Hash codes are not equal", user1.hashCode(), OsmUser.NO_USER.hashCode());
	}
	
	
	/**
	 * Ensure that the object doesn't allow a null user name.
	 */
	@Test(expected=NullPointerException.class)
	public final void testGetInstancePreventsNullUser() {
		OsmUser.getInstance(null, 1);
	}
	
	
	/**
	 * Ensure equal instances are returned for the same input values.
	 */
	@Test
	public final void testGetInstanceSingleObjectReuse() {
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
	public final void testGetInstanceObjectUniqueness() {
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
	public final void testGetInstanceFromStore() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StoreWriter sw = new DataOutputStoreWriter(new DataOutputStream(out));
		StoreClassRegister scr = new StoreClassRegister();
		OsmUser user1 = OsmUser.getInstance("aUser", 12);
		OsmUser user3 = OsmUser.getInstance("aUser2", 13);
		OsmUser user5 = OsmUser.getInstance("", 14);
		user1.store(sw, scr);
		user3.store(sw, scr);
		user5.store(sw, scr);
		StoreReader sr = new DataInputStoreReader(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		OsmUser user2 = OsmUser.getInstance(sr, scr);
		OsmUser user4 = OsmUser.getInstance(sr, scr);
		OsmUser user6 = OsmUser.getInstance(sr, scr);
		assertEquals("Object not equal after retrieval from store", user1, user2);
		assertEquals("Object not equal after retrieval from store", user3, user4);
		assertEquals("Object not equal after retrieval from store", user5, user6);
	}
}
