// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.common;

import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * A timestamp container implementation that holds a timestamp in textual form.
 * 
 * @author Brett Henderson
 */
public class UnparsedTimestampContainer implements TimestampContainer {
	
	private TimestampFormat managedTimestampFormat;
	private String timestampString;
	private Date timestamp;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param timestampFormat
	 *            The format to use for parsing the timestamp string.
	 * @param timestampString
	 *            The timestamp in unparsed string form.
	 */
	public UnparsedTimestampContainer(TimestampFormat timestampFormat, String timestampString) {
		this.managedTimestampFormat = timestampFormat;
		this.timestampString = timestampString;
		
		if (timestampString == null) {
			throw new OsmosisRuntimeException("The entity timestamp attribute is missing.");
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedTimestamp(TimestampFormat timestampFormat) {
		if (timestampString != null && managedTimestampFormat.isEquivalent(timestampFormat)) {
			return timestampString;
		}
		
		// Ensure the timestamp has been parsed.
		getTimestamp();
		
		if (timestamp != null) {
			return timestampFormat.formatTimestamp(timestamp);
		} else {
			return "";
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getTimestamp() {
		if (timestamp == null && timestampString != null && timestampString.length() > 0) {
			timestamp = managedTimestampFormat.parseTimestamp(timestampString);
		}
		
		return timestamp;
	}
}
