// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.common;

import java.util.Date;


/**
 * Concrete implementations of this class support dates in a specific format.
 * This is used for the lazy timestamp parsing functionality whereby dates can
 * be passed through the entire pipeline in unparsed string form if both ends
 * use the same date format.
 * <p>
 * Note that all methods within this class must be threadsafe because it may be
 * utilised concurrently at many points throughout a processing pipeline.
 * 
 * @author Brett Henderson
 */
public abstract class TimestampFormat {
	
	/**
	 * Formats the date object into string form.
	 * 
	 * @param timestamp
	 *            The date to be formatted.
	 * @return The formatted date string.
	 */
	public abstract String formatTimestamp(Date timestamp);
	
	
	/**
	 * Parses a date string into date form.
	 * 
	 * @param timestamp
	 *            The date string to be parsed.
	 * @return The date object.
	 */
	public abstract Date parseTimestamp(String timestamp);
	
	
	/**
	 * Indicates if the specified date format object supports the same date
	 * format as this object.
	 * 
	 * @param timestampFormat
	 *            The date format to compare against.
	 * @return True if the date format is equivalent between the two date
	 *         objects.
	 */
	public boolean isEquivalent(TimestampFormat timestampFormat) {
		return getClass().equals(timestampFormat.getClass());
	}
}
