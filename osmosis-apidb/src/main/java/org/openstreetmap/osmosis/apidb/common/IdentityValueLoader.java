// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.common;

import org.openstreetmap.osmosis.core.lifecycle.Releasable;


/**
 * Retrieves last inserted identity columns. This examines global connection values and may not work
 * correctly if the database uses triggers. It will however work correctly in a multi-threaded
 * environment.
 * 
 * @author Brett Henderson
 */
public interface IdentityValueLoader extends Releasable {

	/**
	 * Returns the id of the most recently inserted row on the current
	 * connection.
	 * 
	 * @return The newly inserted id.
	 */
	long getLastInsertId();


	/**
	 * Returns the most recently returned value from the specified sequence on the current
	 * connection.
	 * 
	 * @param sequenceName
	 *            The name of the sequence to query.
	 * @return The most recent sequence id.
	 */
	long getLastSequenceId(String sequenceName);
}
