// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;


/**
 * Converts a double coordinate value into an equivalent integer with fixed
 * precision.
 * 
 * @author Brett Henderson
 */
public final class FixedPrecisionCoordinateConvertor {
	private static final int PRECISION = 10;
	private static final long MULTIPLICATION_FACTOR = calculateMultiplicationFactor();
	
	
	/**
	 * This class cannot be instantiated.
	 */
	private FixedPrecisionCoordinateConvertor() {
		// Do nothing.
	}
	
	
	/**
	 * Generates the multiplication factor that the double coordinate must be
	 * multiplied by to turn it into a fixed precision integer.
	 * 
	 * @return The double to fixed multiplication factor.
	 */
	private static long calculateMultiplicationFactor() {
		long result;
		
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
	public static long convertToFixed(double coordinate) {
		long result;
		
		result = (long) Math.round(coordinate * MULTIPLICATION_FACTOR);
		
		return result;
	}
	
	
	/**
	 * Converts the requested coordinate from fixed to double precision.
	 * 
	 * @param coordinate
	 *            The fixed coordinate value.
	 * @return The double coordinate value.
	 */
	public static double convertToDouble(long coordinate) {
		double result;
		
		result = ((double) coordinate) / MULTIPLICATION_FACTOR;
		
		return result;
	}
}
