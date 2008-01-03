// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_5;

import java.awt.geom.Area;
import java.io.File;

import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.filter.common.PolygonFileReader;


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
	 * @param completeWays
	 *            Include all nodes for ways which have at least one node inside the filtered area.
	 * @param completeRelations
	 *            Include all relations referenced by other relations which have members inside
	 *            the filtered area.
	 */
	public PolygonFilter(IdTrackerType idTrackerType, File polygonFile, boolean completeWays, boolean completeRelations) {
	    	super(idTrackerType, completeWays, completeRelations);
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
