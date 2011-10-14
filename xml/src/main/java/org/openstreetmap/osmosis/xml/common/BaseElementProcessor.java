// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import java.util.Calendar;

import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.domain.common.UnparsedTimestampContainer;


/**
 * Provides common functionality shared by element processor implementations.
 * 
 * @author Brett Henderson
 */
public abstract class BaseElementProcessor implements ElementProcessor {
	private BaseElementProcessor parentProcessor;
	private ElementProcessor dummyChildProcessor;
	private TimestampFormat timestampFormat;
	private TimestampContainer dummyTimestampContainer;
	private boolean enableDateParsing;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	protected BaseElementProcessor(BaseElementProcessor parentProcessor, boolean enableDateParsing) {
		this.parentProcessor = parentProcessor;
		this.enableDateParsing = enableDateParsing;
		
		if (enableDateParsing) {
			timestampFormat = new XmlTimestampFormat();
		} else {
			Calendar calendar;
			
			calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			dummyTimestampContainer = new SimpleTimestampContainer(calendar.getTime());
		}
	}
	
	
	/**
	 * This implementation returns a dummy element processor as the child which
	 * ignores all nested xml elements. Sub-classes wishing to handle child
	 * elements must override this method and delegate to this method for xml
	 * elements they don't care about.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return A dummy element processor.
	 */
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (dummyChildProcessor == null) {
			dummyChildProcessor = new DummyElementProcessor(this);
		}
		
		return dummyChildProcessor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ElementProcessor getParent() {
		return parentProcessor;
	}
	
	
	/**
	 * Parses a date using the standard osm date format.
	 * 
	 * @param data
	 *            The date string to be parsed.
	 * @return The parsed date (if dateparsing is enabled).
	 */
	protected TimestampContainer createTimestampContainer(String data) {
		if (enableDateParsing) {
			return new UnparsedTimestampContainer(timestampFormat, data);
		} else {
			return dummyTimestampContainer;
		}
	}
}
