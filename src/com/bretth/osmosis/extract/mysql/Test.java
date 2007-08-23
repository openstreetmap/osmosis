package com.bretth.osmosis.extract.mysql;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.bretth.osmosis.core.xml.impl.DateFormatter;
import com.bretth.osmosis.core.xml.impl.DateParser;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	private static final int ITERATIONS = 1000000;
	
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		Date inputDate = new Date(1190535943000l);
		Date outputDate = null;
		//String dateString = "2005-07-26T03:09:29.000Z";
		//String dateString = "2007-01-01T00:00:00.000+11:00";
		//String dateString = "2005-07-26 03:09:29";
		String dateString = new DateFormatter().format(inputDate);
		DateParser dateParser = new DateParser();
		Date beginTimestamp;
		Date endTimestamp;
		long duration;
		double callsPerSecond;
		
		System.out.println("Input date: " + inputDate);
		System.out.println("Time: " + inputDate.getTime());
		System.out.println("Date String: " + dateString);
		
		beginTimestamp = new Date();
		for (int i = 0; i < ITERATIONS; i++) {
			outputDate = dateParser.parse(dateString);
		}
		endTimestamp = new Date();
		
		duration = endTimestamp.getTime() - beginTimestamp.getTime();
		callsPerSecond = 1000 * ITERATIONS / duration;
		
		System.out.println(callsPerSecond + " calls/second");
		System.out.println("Output date: " + outputDate);
		System.out.println("Time: " + outputDate.getTime());
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(outputDate);
		System.out.println("Millisecond: " + calendar.get(Calendar.MILLISECOND));
	}

}
