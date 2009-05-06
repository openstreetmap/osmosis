// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.common;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;


/**
 * A special character set to work around the production OSM database issue of
 * double encoded data.
 * 
 * @author Brett Henderson
 */
public class ProductionDbCharset extends Charset {
	
	private static final String CHARSET_NAME = "ProductionOsmDb";
	
	
	/**
	 * Creates a new instance.
	 */
	public ProductionDbCharset() {
		super(CHARSET_NAME, new String[0]);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Charset cs) {
		return CHARSET_NAME.equals(cs.displayName());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharsetDecoder newDecoder() {
		return new ProductionDbDataDecoder(this, 1, 1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharsetEncoder newEncoder() {
		return new ProductionDbDataEncoder(this, 1, 1);
	}
}
