package com.bretth.osmosis.extract.mysql;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
		DecimalFormat decimalFormat;
		double myValue1;
		double myValue2;
		
		decimalFormat = new DecimalFormat("0.####################E000;-0.####################E000");
		myValue1 = -1234567890.1234567890;
		myValue2 = -myValue1;
		
		System.out.println(myValue1);
		System.out.println(myValue2);
		System.out.println(decimalFormat.format(myValue1));
		System.out.println(decimalFormat.format(myValue2));
		
		System.out.println(Double.parseDouble("0.4890E+02"));
		System.out.println(Double.parseDouble("0.4890E02"));
	}

}
