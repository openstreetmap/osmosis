// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.util;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Contains utility methods supporting storage of ints as chars.
 * 
 * @author Brett Henderson
 */
public final class IntAsChar {
	
	/**
	 * This class cannot be constructed.
	 */
	private IntAsChar() {
		// Do nothing.
	}
	
	
	/**
	 * Converts the specified int to an char and verifies that it is legal.
	 * 
	 * @param value
	 *            The identifier to be converted.
	 * @return The integer representation of the id.
	 */
	public static char intToChar(int value) {
		// Verify that the bit can be safely cast to an integer.
		if (value > Character.MAX_VALUE) {
			throw new OsmosisRuntimeException("Cannot represent " + value + " as a char.");
		}
		if (value < Character.MIN_VALUE) {
			throw new OsmosisRuntimeException("Cannot represent " + value + " as a char.");
		}
		
		return (char) value;
	}
}
