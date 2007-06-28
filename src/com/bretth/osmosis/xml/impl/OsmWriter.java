package com.bretth.osmosis.xml.impl;

import java.io.BufferedWriter;

import com.bretth.osmosis.container.EntityContainer;
import com.bretth.osmosis.container.EntityProcessor;
import com.bretth.osmosis.container.NodeContainer;
import com.bretth.osmosis.container.SegmentContainer;
import com.bretth.osmosis.container.WayContainer;


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
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void begin(BufferedWriter writer) {
		beginOpenElement(writer);
		addAttribute(writer, "version", "0.3");
		addAttribute(writer, "generator", "Osmosis");
		endOpenElement(writer, false);
	}
	
	
	/**
	 * Ends an element.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 */
	public void end(BufferedWriter writer) {
		closeElement(writer);
	}
	
	
	/**
	 * Writes the element in the container.
	 * 
	 * @param writer
	 *            The writer to send the xml to.
	 * @param entityContainer
	 *            The container holding the entity.
	 */
	public void process(BufferedWriter writer, EntityContainer entityContainer) {
		subElementWriter.setWriter(writer);
		entityContainer.process(subElementWriter);
	}
	
	
	/**
	 * Directs data to the appropriate underlying element writer.
	 * 
	 * @author Brett Henderson
	 */
	private class SubElementWriter implements EntityProcessor {
		private BufferedWriter writer;
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
		public void setWriter(BufferedWriter writer) {
			this.writer = writer;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(NodeContainer node) {
			nodeWriter.process(writer, node.getEntity());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(SegmentContainer segment) {
			segmentWriter.process(writer, segment.getEntity());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(WayContainer way) {
			wayWriter.process(writer, way.getEntity());
		}
	}
}
