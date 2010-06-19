// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the transaction snapshot class.
 */
public class TransactionSnapshotTest {
	/**
	 * Tests the database snapshot string parsing.
	 */
	@Test
	public void testParseSnapshot() {
		TransactionSnapshot snapshot;
		
		snapshot = new TransactionSnapshot("1234:5678:101112,131415,161718");

		Assert.assertEquals("xMin is incorrect.", 1234, snapshot.getXMin());
		Assert.assertEquals("xMax is incorrect.", 5678, snapshot.getXMax());
		Assert.assertEquals("xIpList is incorrect.",
				Arrays.asList(new Long[] {
						new Long(101112), new Long(131415),
						new Long(161718) }),
				snapshot.getXIpList());
	}
}
