// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A mocked system time loader allowing canned times to be returned.
 */
public class MockSystemTimeLoader implements SystemTimeLoader {

	private List<Date> times = new ArrayList<Date>();


	/**
	 * Gets the currently available times.
	 * 
	 * @return The times.
	 */
	public List<Date> getTimes() {
		return times;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getSystemTime() {
		return times.remove(0);
	}
}
