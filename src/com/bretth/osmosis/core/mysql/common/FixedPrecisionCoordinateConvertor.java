// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.common;


/**
 * Converts a double coordinate value into an equivalent integer with fixed
 * precision.
 * 
 * @author Brett Henderson
 */
public class FixedPrecisionCoordinateConvertor {
	private static final int PRECISION = 7;
	private static final int MULTIPLICATION_FACTOR = calculateMultiplicationFactor();
	
	
	/**
	 * Generates the multiplication factor that the double coordinate must be
	 * multiplied by to turn it into a fixed precision integer.
	 * 
	 * @return The double to fixed multiplication factor.
	 */
	private static int calculateMultiplicationFactor() {
		int result;
		
		result = 1;
		
		for (int i = 0; i < PRECISION; i++) {
			result *= 10;
		}
		
		return result;
	}
	
	
	/**
	 * Converts the requested coordinate from double to fixed precision.
	 * 
	 * @param coordinate
	 *            The double coordinate value.
	 * @return The fixed coordinate value.
	 */
	public int convertToFixed(double coordinate) {
		int result;
		
		result = (int) Math.round(coordinate * MULTIPLICATION_FACTOR);
		
		return result;
	}
	
	
	/**
	 * Converts the requested coordinate from fixed to double precision.
	 * 
	 * @param coordinate
	 *            The fixed coordinate value.
	 * @return The double coordinate value.
	 */
	public double convertToDouble(int coordinate) {
		double result;
		
		result = ((double) coordinate) / MULTIPLICATION_FACTOR;
		
		return result;
	}
}
