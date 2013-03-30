// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.replication.v0_6.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * This class will generate changeset filenames.
 * 
 * @author Brett Henderson
 */
public class ChangesetFileNameFormatter {
	private DateFormat beginDateFormat;
	private DateFormat endDateFormat;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param beginFileNameFormat
	 *            The date format of the beginning of the filename.
	 * @param endFileNameFormat
	 *            The date format of the end of the filename.
	 */
	public ChangesetFileNameFormatter(String beginFileNameFormat, String endFileNameFormat) {
		TimeZone utcTimezone;
		
		utcTimezone = TimeZone.getTimeZone("UTC");
		beginDateFormat = new SimpleDateFormat(beginFileNameFormat, Locale.US);
		endDateFormat = new SimpleDateFormat(endFileNameFormat, Locale.US);
		beginDateFormat.setTimeZone(utcTimezone);
		endDateFormat.setTimeZone(utcTimezone);
	}
	
	
	/**
	 * Generates an appropriate file name based on the input dates.
	 * 
	 * @param intervalBegin
	 *            The beginning of the time interval.
	 * @param intervalEnd
	 *            The end of the time interval.
	 * @return The file name.
	 */
	public String generateFileName(Date intervalBegin, Date intervalEnd) {
		String fileName;
		
		fileName =
			beginDateFormat.format(intervalBegin)
			+ "-"
			+ endDateFormat.format(intervalEnd)
			+ ".osc.gz";
		
		return fileName;
	}
}
