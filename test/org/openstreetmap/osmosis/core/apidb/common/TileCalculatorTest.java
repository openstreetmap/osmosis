// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.apidb.common;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the quad tile calculator.
 * 
 * @author Brett Henderson
 */
public class TileCalculatorTest {
	/**
	 * Basic test.
	 */
	@Test
	public void test() {
		Assert.assertEquals(
				"Incorrect tile value generated.",
				2062265654,
				new TileCalculator().calculateTile(51.4781325, -0.1474929));
	}
}
