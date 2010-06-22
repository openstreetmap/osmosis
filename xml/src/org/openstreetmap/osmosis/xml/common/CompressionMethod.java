// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;


/**
 * Defines the various compression methods supported by xml tasks.
 * 
 * @author Brett Henderson
 */
public enum CompressionMethod {
	/**
	 * Specifies that no compression be performed.
	 */
	None,
	
	/**
	 * Specifies that GZip compression should be used.
	 */
	GZip,
	
	/**
	 * Specifies that BZip2 compression should be used.
	 */
	BZip2
}
