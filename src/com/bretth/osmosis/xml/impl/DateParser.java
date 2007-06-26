package com.bretth.osmosis.xml.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Handles a number of different date formats encountered in OSM. This is built
 * based on similar code in JOSM. This class is not threadsafe, a separate
 * instance must be created per thread.
 * 
 * @author Brett Henderson
 */
public class DateParser {
	
	private static final String[] formats = {
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
	private Pattern pattern;
	
	
	/**
	 * Creates a new instance.
	 */
	public DateParser() {
		// Build a list of candidate date parsers.
		dateParsers = new ArrayList<DateFormat>(formats.length);
		for (int i = 0; i < formats.length; i++) {
			dateParsers.add(new SimpleDateFormat(formats[i]));
		}
		
		pattern = Pattern.compile("(....-..-..T..:..:..[+-]..):(..)");
	}
	
	
	/**
	 * Attempts to parse the specified date.
	 * 
	 * @param date
	 *            The date to parse.
	 * @return The date.
	 * @throws ParseException
	 *             Occurs if the date does not match any of the supported date
	 *             formats.
	 */
	public Date parse(String date) {
		Matcher matcher;
		String correctedDate;
		
		// first try to fix ruby's broken xmlschema - format
		matcher = pattern.matcher(date);
		if (matcher.matches()) {
			correctedDate = matcher.group(1) + matcher.group(2);
		} else {
			correctedDate = date;
		}
		
		// Try the date parsers one by one until a suitable format is found.
		for (DateFormat dateParser : dateParsers) {
			try {
				return dateParser.parse(correctedDate);
			} catch (ParseException pe) {
				// Ignore parsing errors and try the next pattern.
			}
		}
		
		throw new OsmosisRuntimeException("The date string (" + date + ") could not be parsed.");
	}
}
