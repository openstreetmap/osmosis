// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

/**
 * Tests the list id tracker implementation.
 */
public class ListIdTrackerTest extends IdTrackerBase {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IdTracker getImplementation() {
		return new ListIdTracker();
	}
}
