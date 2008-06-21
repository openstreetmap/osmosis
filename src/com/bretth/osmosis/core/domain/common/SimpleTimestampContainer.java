// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.common;

import java.util.Date;


/**
 * A timestamp container implementation that holds a standard date object.
 * 
 * @author Brett Henderson
 */
public class SimpleTimestampContainer implements TimestampContainer {
	
	private Date timestamp;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param timestamp
	 *            The timestamp to be managed.
	 */
	public SimpleTimestampContainer(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedTimestamp(TimestampFormat timestampFormat) {
		return timestampFormat.formatTimestamp(timestamp);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getTimestamp() {
		return timestamp;
	}
}
