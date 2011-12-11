// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.Map;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;
import org.openstreetmap.osmosis.xml.common.ElementProcessor;


/**
 * Provides an element processor implementation for an osm change element.
 * 
 * @author Brett Henderson
 */
public class ChangeSourceElementProcessor extends BaseElementProcessor {
	private static final String ELEMENT_NAME_CREATE = "create";
	private static final String ELEMENT_NAME_MODIFY = "modify";
	private static final String ELEMENT_NAME_DELETE = "delete";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
	
	
	private OsmElementProcessor createElementProcessor;
	private OsmElementProcessor modifyElementProcessor;
	private OsmElementProcessor deleteElementProcessor;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param changeSink
	 *            The changeSink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	public ChangeSourceElementProcessor(
			BaseElementProcessor parentProcessor, ChangeSink changeSink, boolean enableDateParsing) {
		super(parentProcessor, enableDateParsing);
		
		createElementProcessor =
			new OsmElementProcessor(
					this, new ChangeSinkAdapter(changeSink, ChangeAction.Create), enableDateParsing, false);
		modifyElementProcessor =
			new OsmElementProcessor(
					this, new ChangeSinkAdapter(changeSink, ChangeAction.Modify), enableDateParsing, false);
		deleteElementProcessor =
			new OsmElementProcessor(
					this, new ChangeSinkAdapter(changeSink, ChangeAction.Delete), enableDateParsing, false);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		String fileVersion;
		
		fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		
		if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
			System.err.println(
				"Warning, expected version " + XmlConstants.OSM_VERSION
				+ " but received " + fileVersion + "."
			);
		}
	}
	
	
	/**
	 * Retrieves the appropriate child element processor for the newly
	 * encountered nested element.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return The appropriate element processor for the nested element.
	 */
	@Override
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (ELEMENT_NAME_CREATE.equals(qName)) {
			return createElementProcessor;
		} else if (ELEMENT_NAME_MODIFY.equals(qName)) {
			return modifyElementProcessor;
		} else if (ELEMENT_NAME_DELETE.equals(qName)) {
			return deleteElementProcessor;
		}
		
		return super.getChild(uri, localName, qName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		// This class produces no data and therefore doesn't need to do anything
		// when the end of the element is reached.
	}
	
	
	private static class ChangeSinkAdapter implements Sink {
		private ChangeSink changeSink;
		private ChangeAction action;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param changeSink
		 *            The changeSink for receiving processed data.
		 * @param action
		 *            The action to apply to all data received.
		 */
		public ChangeSinkAdapter(ChangeSink changeSink, ChangeAction action) {
			this.changeSink = changeSink;
			this.action = action;
		}
	    
	    
	    /**
	     * {@inheritDoc}
	     */
	    public void initialize(Map<String, Object> metaData) {
			changeSink.initialize(metaData);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(EntityContainer entityContainer) {
			changeSink.process(new ChangeContainer(entityContainer, action));
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void complete() {
			changeSink.complete();
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void release() {
			changeSink.release();
		}
	}
}
