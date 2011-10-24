// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.testutil;

import org.junit.Rule;


/**
 * Convenience base class providing facilities for test data creation and
 * lifecycle management.
 * 
 * @author Brett Henderson
 */
public class AbstractDataTest {
	/**
	 * Manages creation and lifecycle of test data files.
	 */
	@Rule
	public TestDataUtilities dataUtils = new TestDataUtilities();
}
