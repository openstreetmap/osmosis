// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import org.postgis.LinearRing;
import org.postgis.Point;
import org.postgis.Polygon;


/**
 * Builds PostGIS Polygon objects based on a series of points.
 * 
 * @author Brett Henderson
 */
public class PolygonBuilder {
	/**
	 * Creates a PostGIS Polygon object corresponding to the provided Point
	 * list.
	 * 
	 * @param points
	 *            The points to build a polygon from.
	 * @return The Polygon object.
	 */
	public Polygon createPolygon(Point[] points) {
		Polygon result;
		
		result = new Polygon(new LinearRing[] {new LinearRing(points)});
		result.srid = 4326;
		
		return result;
	}
}
