// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.common;

public class BitSetIdTrackerTest extends IdTrackerBase {

	@Override
	protected IdTracker getImplementation() {
		return new BitSetIdTracker();
	}
}
