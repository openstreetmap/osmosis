// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;


/**
 * Defines some common constants shared between various xml processing classes.
 * 
 * @author Brett Henderson
 */
public final class XmlConstants {
	
	/**
	 * This class cannot be instantiated.
	 */
	private XmlConstants() {
	}
	
	
	/**
	 * Defines the version number to be stored in osm xml files. This number
	 * will also be applied to osmChange files.
	 */
	public static final String OSM_VERSION = "0.6";
	
	
	/**
	 * The default URL for the production API.
	 */
	public static final String DEFAULT_URL = "http://www.openstreetmap.org/api/0.6";
}
