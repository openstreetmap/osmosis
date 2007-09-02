package com.bretth.osmosis.core.filter;

import java.awt.geom.Area;
import java.io.File;

import com.bretth.osmosis.core.domain.Node;
import com.bretth.osmosis.core.filter.impl.PolygonFileReader;


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
	 * @param polygonFile
	 *            The file containing the polygon coordinates.
	 */
	public PolygonFilter(File polygonFile) {
		this.polygonFile = polygonFile;
		
		area = null;
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
