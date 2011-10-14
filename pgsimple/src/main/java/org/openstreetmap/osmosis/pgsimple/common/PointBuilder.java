// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import org.postgis.Point;


/**
 * Builds PostGIS Point objects based on a set of coordinates.
 * 
 * @author Brett Henderson
 */
public class PointBuilder {
	/**
	 * Creates a PostGIS Point object corresponding to the provided coordinates.
	 * 
	 * @param latitude
	 *            The latitude measured in degrees.
	 * @param longitude
	 *            The longitude measured in degrees.
	 * @return The Point object.
	 */
	public Point createPoint(double latitude, double longitude) {
		Point result;
		
		result = new Point(longitude, latitude);
		result.srid = 4326;
		
		return result;
	}
}
