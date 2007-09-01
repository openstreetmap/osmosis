package com.bretth.osmosis.core.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osmosis.core.container.EntityContainer;
import com.bretth.osmosis.core.container.EntityProcessor;
import com.bretth.osmosis.core.container.NodeContainer;
import com.bretth.osmosis.core.container.SegmentContainer;
import com.bretth.osmosis.core.container.WayContainer;


/**
 * Renders OSM data types as xml.
 * 
 * @author Brett Henderson
 */
public class OsmWriter extends ElementWriter {
	
	private SubElementWriter subElementWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public OsmWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		// Create the sub-element writer which calls the appropriate element
		// writer based on data type.
		subElementWriter = new SubElementWriter(indentLevel + 1);
	}
	
	
	/**
	 * Begins an element.
	 */
	public void begin() {
		beginOpenElement();
		addAttribute("version", XmlConstants.OSM_VERSION);
		addAttribute("generator", "Osmosis");
		endOpenElement(false);
	}
	
	
	/**
	 * Ends an element.
	 */
	public void end() {
		closeElement();
	}
	
	
	/**
	 * Writes the element in the container.
	 * 
	 * @param entityContainer
	 *            The container holding the entity.
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(subElementWriter);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(BufferedWriter writer) {
		super.setWriter(writer);
		
		// Tell the sub element writer that a new writer is available. This will
		// cause the underlying entity writing classes to be updated.
		subElementWriter.updateWriter();
	}





	/**
	 * Directs data to the appropriate underlying element writer.
	 * 
	 * @author Brett Henderson
	 */
	private class SubElementWriter implements EntityProcessor {
		private NodeWriter nodeWriter;
		private SegmentWriter segmentWriter;
		private WayWriter wayWriter;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param indentLevel
		 *            The indent level of the sub-elements.
		 */
		public SubElementWriter(int indentLevel) {
			nodeWriter = new NodeWriter("node", indentLevel);
			segmentWriter = new SegmentWriter("segment", indentLevel);
			wayWriter = new WayWriter("way", indentLevel);
		}
		
		
		/**
		 * Updates the underlying writer.
		 * 
		 * @param writer
		 *            The writer to be used for all output xml.
		 */
		public void updateWriter() {
			nodeWriter.setWriter(writer);
			segmentWriter.setWriter(writer);
			wayWriter.setWriter(writer);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(NodeContainer node) {
			nodeWriter.process(node.getEntity());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(SegmentContainer segment) {
			segmentWriter.process(segment.getEntity());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(WayContainer way) {
			wayWriter.process(way.getEntity());
		}
	}
}
