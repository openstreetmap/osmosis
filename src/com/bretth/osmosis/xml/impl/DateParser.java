package com.bretth.osmosis.xml.impl;

import java.text.ParseException;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Handles a number of different date formats encountered in OSM. This is built
 * based on similar code in JOSM. This class is not threadsafe, a separate
 * instance must be created per thread.
 * 
 * @author Brett Henderson
 */
public class DateParser {
	private DatatypeFactory datatypeFactory;
	
	
	/**
	 * Creates a new instance.
	 */
	public DateParser() {
		// Build an xml data type factory.
		try {
			datatypeFactory = DatatypeFactory.newInstance();
			
		} catch (DatatypeConfigurationException e) {
			throw new OsmosisRuntimeException("Unable to instantiate xml datatype factory.", e);
		}
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
		return datatypeFactory.newXMLGregorianCalendar(date).toGregorianCalendar().getTime();
	}
}
