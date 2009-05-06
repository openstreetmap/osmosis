// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;


/**
 * Calculates a tile index based upon coordinate values. Note that this class
 * returns a signed integer due to the lack of an unsigned integer type in java.
 * The result is a 32-bit unsigned integer but stored in a long value for ease
 * of use.
 * <p>
 * The result can be cast directly to an int, but converting back to an unsigned
 * long value must be performed like: <code>
 * long tile = intTile & 0xFFFFFFFFl;
 * </code>
 * 
 * @author Brett Henderson
 */
public class TileCalculator {
	
	/**
	 * Calculates a tile index based upon the supplied coordinates.
	 * 
	 * @param latitude
	 *            The coordinate latitude.
	 * @param longitude
	 *            The coordinate longitude.
	 * @return The tile index value.
	 */
	public long calculateTile(double latitude, double longitude) {
		int x;
		int y;
		long tile;
		
		x = (int) Math.round((longitude + 180) * 65535 / 360);
		y = (int) Math.round((latitude + 90) * 65535 / 180);
		
		tile = 0;
		
		for (int i = 15; i >= 0; i--) {
			tile = (tile << 1) | ((x >> i) & 1);
			tile = (tile << 1) | ((y >> i) & 1);
		}
		
		return tile;
	}
}
