// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.Date;


/**
 * Provides {@link Replicator} with access to the system time on the database server. This avoids
 * relying on the clock of this system which may be different.
 */
public interface SystemTimeLoader {
	/**
	 * Gets the system time of the database server.
	 * 
	 * @return The timestamp.
	 */
	Date getSystemTime();
}
