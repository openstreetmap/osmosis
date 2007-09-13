package com.bretth.osmosis.core.xml.common;


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
