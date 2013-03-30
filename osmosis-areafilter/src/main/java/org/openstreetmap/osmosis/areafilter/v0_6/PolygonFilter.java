// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.areafilter.v0_6;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.File;

import org.openstreetmap.osmosis.areafilter.common.PolygonFileReader;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;


/**
 * Provides a filter for extracting all entities that lie within a specific
 * geographical box identified by latitude and longitude coordinates.
 * 
 * @author Brett Henderson
 */
public class PolygonFilter extends AreaFilter {
	
	private File polygonFile;
	private Area area;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 * @param polygonFile
	 *            The file containing the polygon coordinates.
	 * @param clipIncompleteEntities
	 *            If true, entities referring to non-existent entities will be
	 *            modified to ensure referential integrity. For example, ways
	 *            will be modified to only include nodes inside the area.
	 * @param completeWays
	 *            Include all nodes for ways which have at least one node inside the filtered area.
	 * @param completeRelations
	 *            Include all relations referenced by other relations which have members inside
	 *            the filtered area.
     * @param cascadingRelations
     *            Include all relations that reference other relations which have members inside the
     *            filtered area. This is less costly than completeRelations.
	 */
	public PolygonFilter(
			IdTrackerType idTrackerType, File polygonFile, boolean clipIncompleteEntities, boolean completeWays,
			boolean completeRelations, boolean cascadingRelations) {
	    super(idTrackerType, clipIncompleteEntities, completeWays, completeRelations, cascadingRelations);
		this.polygonFile = polygonFile;
		
		area = null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer boundContainer) {
		Bound newBound = null;

		// Configure the area if it hasn't been created yet. (Should this be in an "initialize" method?)
		if (area == null) {
			area = new PolygonFileReader(polygonFile).loadPolygon();
		}
		
		for (Bound b : boundContainer.getEntity().toSimpleBound()) {
			if (newBound == null) {
				newBound = simpleBoundIntersect(b);
			} else {
				newBound = newBound.union(simpleBoundIntersect(b));
			}
		}

		if (newBound != null) {
			super.process(new BoundContainer(newBound));
		}
	}


	/**
	 * Get the simple intersection of this polygon with the passed Bound.
	 * 
	 * @param bound
	 *            Bound with which to intersect. Must be "simple" (not cross antimeridian).
	 * @return Bound resulting rectangular area after intersection
	 */
	private Bound simpleBoundIntersect(Bound bound) {
		Rectangle2D r;
		double width, height;

		Bound newBound = null;
		Area a2 = (Area) area.clone(); // make a copy so we don't disturb the original

		/*
		 * Note that AWT uses the computer graphics convention with the origin at the top left, so
		 * top and bottom are reversed for a Rectangle2D vs. a Bound.
		 */

		if (bound.getLeft() > bound.getRight()) {
			return null;
		}
		width = bound.getRight() - bound.getLeft();
		height = bound.getTop() - bound.getBottom();
		/*
		 * Perform the intersect against the Area itself instead of its bounding box for maximum
		 * precision.
		 */
		a2.intersect(new Area(new Rectangle2D.Double(
		        bound.getLeft(),
		        bound.getBottom(),
		        width,
		        height)));
		if (!a2.isEmpty()) {
			r = a2.getBounds2D();
			newBound = new Bound(
			        r.getMaxX(),
			        r.getMinX(),
			        r.getMaxY(),
			        r.getMinY(),
			        bound.getOrigin());
		}
		return newBound;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isNodeWithinArea(Node node) {
		double latitude;
		double longitude;
		
		// Configure the area if it hasn't been created yet.
		if (area == null) {
			area = new PolygonFileReader(polygonFile).loadPolygon();
		}
		
		latitude = node.getLatitude();
		longitude = node.getLongitude();
		
		return area.contains(longitude, latitude);
	}
}
