package com.bretth.osmosis.extract.mysql;

import com.bretth.osmosis.core.xml.common.DateParser;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	
	private static DateParser parser = new DateParser();
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		test("2007-09-23T08:25:43.000Z");
		test("2007-09-23T08:25:43Z");
	}
	
	
	private static void test(String date) {
		System.out.println("date: " + date + " parsed: " + parser.parse(date));
	}
}
