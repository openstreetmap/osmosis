// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
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
