// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Handles a number of different date FORMATS encountered in OSM. This is built
 * based on similar code in JOSM. This class is not threadsafe, a separate
 * instance must be created per thread.
 * 
 * @author Brett Henderson
 */
public class FallbackDateParser {
	private static final Logger LOG = Logger.getLogger(FallbackDateParser.class.getName());
	
	private static final String[] FORMATS = {
	    "yyyy-MM-dd'T'HH:mm:ss'Z'",
		"yyyy-MM-dd'T'HH:mm:ssZ",
		"yyyy-MM-dd'T'HH:mm:ss",
		"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd HH:mm:ss",
		"MM/dd/yyyy HH:mm:ss",
		"MM/dd/yyyy'T'HH:mm:ss.SSS'Z'",
		"MM/dd/yyyy'T'HH:mm:ss.SSSZ",
		"MM/dd/yyyy'T'HH:mm:ss.SSS",
		"MM/dd/yyyy'T'HH:mm:ssZ",
		"MM/dd/yyyy'T'HH:mm:ss",
		"yyyy:MM:dd HH:mm:ss"
	};
	
	
	private List<DateFormat> dateParsers;
	private int activeDateParser;
	
	
	/**
	 * Creates a new instance.
	 */
	public FallbackDateParser() {
		// Build a list of candidate date parsers.
		dateParsers = new ArrayList<DateFormat>(FORMATS.length);
		for (int i = 0; i < FORMATS.length; i++) {
			dateParsers.add(new SimpleDateFormat(FORMATS[i]));
		}
		
		// We haven't selected a date parser yet.
		activeDateParser = -1;
	}
	
	
	/**
	 * Attempts to parse the specified date.
	 * 
	 * @param date
	 *            The date to parse.
	 * @return The date.
	 */
	public Date parse(String date) {
		String correctedDate;
		
		// Try to fix ruby's broken xmlschema - format
		// Replace this:
		// 2007-02-12T18:43:01+00:00
		// With this:
		// 2007-02-12T18:43:01+0000
		if (date.length() == 25 && date.charAt(22) == ':') {
			correctedDate = date.substring(0, 22) + date.substring(23, 25);
		} else {
			correctedDate = date;
		}
		
		// If we have previously successfully used a date parser, we'll try it
		// first.
		if (activeDateParser >= 0) {
			try {
				return dateParsers.get(activeDateParser).parse(correctedDate);
			} catch (ParseException e) {
				// The currently active parser didn't work, so we must clear it
				// and find a new appropriate parser.
				activeDateParser = -1;
			}
		}
		
		// Try the date parsers one by one until a suitable format is found.
		for (int i = 0; i < dateParsers.size(); i++) {
			try {
				Date result;
				
				// Attempt to parse with the current parser, if successful we
				// store its index for next time.
				result = dateParsers.get(i).parse(correctedDate);
				activeDateParser = i;
				
				return result;
				
			} catch (ParseException e) {
				LOG.log(Level.FINER, "Pattern " + i + " could not parse the date.", e);
				// Ignore parsing errors and try the next pattern.
			}
		}
		
		throw new OsmosisRuntimeException("The date string (" + date + ") could not be parsed.");
	}
}
