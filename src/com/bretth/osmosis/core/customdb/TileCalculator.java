package com.bretth.osmosis.core.customdb;


/**
 * Calculates a tile index based upon coordinate values. Note that this
 * implementation produces different results to the version used for mysql tile
 * indexes. This version produces an integer output that includes negative
 * values as opposed to the unsigned integer used elsewhere.
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
	public int calculateTile(double latitude, double longitude) {
		int x;
		int y;
		int tile;
		
		x = (int) Math.round((longitude) * 65536 / 360);
		y = (int) Math.round((latitude) * 65536 / 180);
		
		tile = 0;
		
		for (int i = 15; i >= 0; i--) {
			tile = (tile << 1) | ((x >> i) & 1);
			tile = (tile << 1) | ((y >> i) & 1);
		}
		
		return tile;
	}
}
