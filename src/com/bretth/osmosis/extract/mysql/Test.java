package com.bretth.osmosis.extract.mysql;

import java.util.Date;

import com.bretth.osmosis.core.xml.impl.DateFormatter;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	private static final int ITERATIONS = 10000000;
	
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		// 2007-09-23T08:25:43.000Z
		Date inputDate = new Date(1190535943001l);
		DateFormatter dateFormatter;
		String outString = null;
		Date beginTimestamp;
		Date endTimestamp;
		long duration;
		double callsPerSecond;
		
		dateFormatter = new DateFormatter();
		
		System.out.println("Input date: " + inputDate);
		System.out.println("Time: " + inputDate.getTime());
		
		beginTimestamp = new Date();
		
		for (int i = 0; i < ITERATIONS; i++) {
			outString = dateFormatter.format(inputDate);
		}
		
		endTimestamp = new Date();
		
		duration = endTimestamp.getTime() - beginTimestamp.getTime();
		callsPerSecond = 1000 * ITERATIONS / duration;
		
		System.out.println(callsPerSecond + " calls/second");
		
		System.out.println("Formatted Date: " + outString);
	}

}
