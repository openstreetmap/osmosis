// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.common;

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
	public Polygon createPolygon(Point points[]) {
		Polygon result;
		
		result = new Polygon(new LinearRing[] {new LinearRing(points)});
		result.srid = 4326;
		
		return result;
	}
}
