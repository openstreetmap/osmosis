// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.common;

import java.util.Date;


/**
 * Defines the interface for a class holding timestamps in one or both of parsed
 * and unparsed forms. This allows a date to remain in text form throughout a
 * pipeline if both ends utilise the date in the same format.
 * 
 * @author Brett Henderson
 */
public interface TimestampContainer {
	
	
	/**
	 * @return The timestamp. 
	 */
	Date getTimestamp();
	
	
	/**
	 * Gets the timestamp in a string format.
	 * 
	 * @param timestampFormat
	 *            The formatter to use for formatting the timestamp into a
	 *            string.
	 * @return The timestamp string.
	 */
	String getFormattedTimestamp(TimestampFormat timestampFormat);
}
