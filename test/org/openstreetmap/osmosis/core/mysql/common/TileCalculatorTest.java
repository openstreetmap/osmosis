// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.common;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.util.TileCalculator;


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
