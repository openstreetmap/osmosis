// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5.impl;


/**
 * Defines some common constants shared between various xml processing classes.
 * 
 * @author Brett Henderson
 */
public interface XmlConstants {
	/**
	 * Defines the version number to be stored in osm xml files. This number
	 * will also be applied to osmChange files.
	 */
	public static final String OSM_VERSION = "0.5";
	
	
	/**
	 * The default URL for the production API.
	 */
	public static final String DEFAULT_URL = "http://www.openstreetmap.org/api/0.5";
}
