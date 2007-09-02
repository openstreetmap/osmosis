package com.bretth.osmosis.core.filter;

import com.bretth.osmosis.core.domain.Node;


/**
 * Provides a filter for extracting all entities that lie within a specific
 * geographical box identified by latitude and longitude coordinates.
 * 
 * @author Brett Henderson
 */
public class BoundingBoxFilter extends AreaFilter {
	private double left;
	private double right;
	private double top;
	private double bottom;
	
	
	/**
	 * Creates a new instance with the specified geographical coordinates. When
	 * filtering, nodes right on the edge of the box will be included.
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
	public BoundingBoxFilter(double left, double right, double top, double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isNodeWithinArea(Node node) {
		double latitude;
		double longitude;
		
		latitude = node.getLatitude();
		longitude = node.getLongitude();
		
		return top >= latitude && bottom <= latitude && left <= longitude && right >= longitude;
	}
}
