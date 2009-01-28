// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.common;

import java.util.Date;

import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;


/**
 * A timestamp format implementation for dates read and stored from osm xml
 * files.
 * 
 * @author Brett Henderson
 */
public class XmlTimestampFormat extends TimestampFormat {
	
	private ThreadLocal<DateFormatter> dateFormatterStore;
	private ThreadLocal<DateParser> dateParserStore;
	
	
	/**
	 * Creates a new instance.
	 */
	public XmlTimestampFormat() {
		dateFormatterStore = new ThreadLocal<DateFormatter>();
		dateParserStore = new ThreadLocal<DateParser>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String formatTimestamp(Date timestamp) {
		DateFormatter dateFormatter;
		
		dateFormatter = dateFormatterStore.get();
		if (dateFormatter == null) {
			dateFormatter = new DateFormatter();
			dateFormatterStore.set(dateFormatter);
		}
		
		return dateFormatter.format(timestamp);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date parseTimestamp(String timestamp) {
		DateParser dateParser;
		
		dateParser = dateParserStore.get();
		if (dateParser == null) {
			dateParser = new DateParser();
			dateParserStore.set(dateParser);
		}
		
		return dateParser.parse(timestamp);
	}
}
