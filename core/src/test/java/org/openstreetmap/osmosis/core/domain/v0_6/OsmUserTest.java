// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.store.DataInputStoreReader;
import org.openstreetmap.osmosis.core.store.DataOutputStoreWriter;
import org.openstreetmap.osmosis.core.store.DynamicStoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;


/**
 * Tests the OsmUser class.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUserTest {
	
	
	/**
	 * Verify the details of the NONE user.
	 */
	@Test
	public final void testGetInstanceNoUser() {
		assertEquals("None user id is incorrect.", -1, OsmUser.NONE.getId());
		assertEquals("None user name is incorrect.", "", OsmUser.NONE.getName());
	}
	
	
	/**
	 * Ensure that the class doesn't allow a null user name.
	 */
	@Test(expected = NullPointerException.class)
	public final void testGetInstancePreventsNullUser() {
		new OsmUser(1, null);
	}
	
	
	/**
	 * Ensure that the class doesn't allow the reserved "NONE" user id to be specified.
	 */
	@Test(expected = OsmosisRuntimeException.class)
	public final void testGetInstancePreventsNoneUser() {
		new OsmUser(OsmUser.NONE.getId(), "MyNoneUser");
	}
	
	
	/**
	 * Ensure the instance is correctly written to and read from the store.
	 */
	@Test
	public final void testGetInstanceFromStore() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StoreWriter sw = new DataOutputStoreWriter(new DataOutputStream(out));
		StoreClassRegister scr = new DynamicStoreClassRegister();
		OsmUser user1 = new OsmUser(12, "aUser");
		OsmUser user3 = new OsmUser(13, "aUser2");
		OsmUser user5 = new OsmUser(14, "");
		user1.store(sw, scr);
		user3.store(sw, scr);
		user5.store(sw, scr);
		StoreReader sr = new DataInputStoreReader(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		OsmUser user2 = new OsmUser(sr, scr);
		OsmUser user4 = new OsmUser(sr, scr);
		OsmUser user6 = new OsmUser(sr, scr);
		assertEquals("Object not equal after retrieval from store", user1, user2);
		assertEquals("Object not equal after retrieval from store", user3, user4);
		assertEquals("Object not equal after retrieval from store", user5, user6);
	}
}
