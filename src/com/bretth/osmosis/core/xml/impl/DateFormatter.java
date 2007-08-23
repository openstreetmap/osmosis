package com.bretth.osmosis.core.xml.impl;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Outputs a date in a format suitable for an OSM XML file.
 * 
 * @author Brett Henderson
 */
public class DateFormatter {
	
	private GregorianCalendar calendar;
	private DatatypeFactory datatypeFactory;
	
	
	/**
	 * Creates a new instance.
	 */
	public DateFormatter() {
		calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		
		try {
			datatypeFactory = DatatypeFactory.newInstance();
			
		} catch (DatatypeConfigurationException e) {
			throw new OsmosisRuntimeException("Unable to instantiate a new XML datatype factory.", e);
		}
	}
	
	
	/**
	 * Formats a date in XML format.
	 * 
	 * @param date
	 *            The date to be formatted.
	 * @return The string representing the date.
	 */
	public String format(Date date) {
		XMLGregorianCalendar xmlCalendar;
		
		calendar.setTime(date);
		
		xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
		
		return xmlCalendar.toXMLFormat();
	}
}
