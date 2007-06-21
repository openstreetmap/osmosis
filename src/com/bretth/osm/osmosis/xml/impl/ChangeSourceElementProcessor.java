package com.bretth.osm.osmosis.xml.impl;

import org.xml.sax.Attributes;

import com.bretth.osm.osmosis.data.Node;
import com.bretth.osm.osmosis.data.Segment;
import com.bretth.osm.osmosis.data.Way;
import com.bretth.osm.osmosis.task.ChangeAction;
import com.bretth.osm.osmosis.task.ChangeSink;
import com.bretth.osm.osmosis.task.Sink;


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
	private static final String ATTRIBUTE_VALUE_VERSION = "0.3";
	
	
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
	 */
	public ChangeSourceElementProcessor(BaseElementProcessor parentProcessor, ChangeSink changeSink) {
		super(parentProcessor);
		
		createElementProcessor =
			new OsmElementProcessor(this, new ChangeSinkAdapter(changeSink, ChangeAction.Create));
		modifyElementProcessor =
			new OsmElementProcessor(this, new ChangeSinkAdapter(changeSink, ChangeAction.Modify));
		deleteElementProcessor =
			new OsmElementProcessor(this, new ChangeSinkAdapter(changeSink, ChangeAction.Delete));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		String fileVersion;
		
		fileVersion = attributes.getValue(ATTRIBUTE_NAME_VERSION);
		
		if (!ATTRIBUTE_VALUE_VERSION.equals(fileVersion)) {
			System.err.println(
				"Warning, expected version " + ATTRIBUTE_VALUE_VERSION
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
		public void processNode(Node node) {
			changeSink.processNode(node, action);
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void processSegment(Segment segment) {
			changeSink.processSegment(segment, action);
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void processWay(Way way) {
			changeSink.processWay(way, action);
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
