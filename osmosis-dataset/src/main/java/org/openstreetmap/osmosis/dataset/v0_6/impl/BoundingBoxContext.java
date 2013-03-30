// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;

import org.openstreetmap.osmosis.core.filter.common.BitSetIdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.store.UnsignedIntegerComparator;
import org.openstreetmap.osmosis.core.util.TileCalculator;


/**
 * Contains the data associated with a bounding box iteration call on a dataset.
 * 
 * @author Brett Henderson
 */
public class BoundingBoxContext {
	/**
	 * The coordinates of the box represented as an AWT box for use in AWT API
	 * calls.
	 */
	public final Rectangle2D boundingBox;
	/**
	 * The maximum tile value for the box.
	 */
	public final int maximumTile;
	/**
	 * The minimum tile value for the box.
	 */
	public final int minimumTile;
	/**
	 * All node ids are stored within this tracker.
	 */
	public final IdTracker nodeIdTracker;
	/**
	 * All way ids are stored within this tracker.
	 */
	public final IdTracker wayIdTracker;
	/**
	 * All relation ids are stored within this tracker.
	 */
	public final IdTracker relationIdTracker;
	/**
	 * All nodes outside the bounding box are stored within this tracker.
	 */
	public final IdTracker externalNodeIdTracker;
	
	
	/**
	 * Creates a new instance. This initialises all internal variables so that
	 * no public variables require initialisation by external code.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 */
	public BoundingBoxContext(double left, double right, double top, double bottom) {
		TileCalculator tileCalculator;
		Comparator<Integer> tileOrdering;
		int calculatedTile;
		int tmpMinimumTile;
		int tmpMaximumTile;
		
		// Create utility objects.
		tileCalculator = new TileCalculator();
		tileOrdering = new UnsignedIntegerComparator();
		
		// Create a rectangle representing the bounding box.
		boundingBox = new Rectangle2D.Double(left, bottom, right - left, top - bottom);
		
		// Calculate the maximum and minimum tile values for the bounding box.
		calculatedTile = (int) tileCalculator.calculateTile(top, left);
		tmpMaximumTile = calculatedTile;
		tmpMinimumTile = calculatedTile;
		
		calculatedTile = (int) tileCalculator.calculateTile(top, right);
		if (tileOrdering.compare(calculatedTile, tmpMinimumTile) < 0) {
			tmpMinimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, tmpMaximumTile) > 0) {
			tmpMaximumTile = calculatedTile;
		}
		
		calculatedTile = (int) tileCalculator.calculateTile(bottom, left);
		if (tileOrdering.compare(calculatedTile, tmpMinimumTile) < 0) {
			tmpMinimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, tmpMaximumTile) > 0) {
			tmpMaximumTile = calculatedTile;
		}
		
		calculatedTile = (int) tileCalculator.calculateTile(bottom, right);
		if (tileOrdering.compare(calculatedTile, tmpMinimumTile) < 0) {
			tmpMinimumTile = calculatedTile;
		}
		if (tileOrdering.compare(calculatedTile, tmpMaximumTile) > 0) {
			tmpMaximumTile = calculatedTile;
		}
		
		// The tile values at the corners are all zero. If max tile is 0 but if
		// the maximum longitude and latitude are above minimum values set the
		// maximum tile to the maximum value.
		if (tmpMaximumTile == 0) {
			if (right > -180 || top > -90) {
				tmpMaximumTile = 0xFFFFFFFF;
			}
		}
		
		// Set the "final" versions of tile variables with the results.
		maximumTile = tmpMaximumTile;
		minimumTile = tmpMinimumTile;
		
		// Create the id trackers.
		nodeIdTracker = new BitSetIdTracker();
		wayIdTracker = new BitSetIdTracker();
		relationIdTracker = new BitSetIdTracker();
		externalNodeIdTracker = new BitSetIdTracker();
	}
}
