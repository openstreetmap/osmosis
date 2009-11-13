// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_5.impl;

import java.io.BufferedWriter;
import java.io.Writer;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.xml.common.ElementWriter;


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
		addAttribute("generator", "Osmosis " + OsmosisConstants.VERSION);
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
	public void setWriter(final Writer writer) {
		super.setWriter(writer);
		
		// Tell the sub element writer that a new writer is available. This will
		// cause the underlying entity writing classes to be updated.
		subElementWriter.updateWriter(writer);
	}
	
	
	/**
	 * Directs data to the appropriate underlying element writer.
	 * 
	 * @author Brett Henderson
	 */
	private static class SubElementWriter implements EntityProcessor {
		private NodeWriter nodeWriter;
		private WayWriter wayWriter;
		private RelationWriter relationWriter;
		private BoundWriter boundWriter;
		private boolean boundWritten = false; // can't write a Bound twice
		private boolean entitiesWritten = false; // can't write a Bound after any Entities
		
		/**
		 * Creates a new instance.
		 * 
		 * @param indentLevel
		 *            The indent level of the sub-elements.
		 */
		public SubElementWriter(int indentLevel) {
			nodeWriter = new NodeWriter("node", indentLevel);
			wayWriter = new WayWriter("way", indentLevel);
			relationWriter = new RelationWriter("relation", indentLevel);
			boundWriter = new BoundWriter("bound", indentLevel);
		}
		
		
		/**
		 * Updates the underlying writer.
		 * 
		 * @param writer
		 *            The writer to be used for all output xml.
		 */
		public void updateWriter(final Writer writer) {
			nodeWriter.setWriter(writer);
			wayWriter.setWriter(writer);
			relationWriter.setWriter(writer);
			boundWriter.setWriter(writer);
			// reset the flags indicating which data has been written
			boundWritten = false;
			entitiesWritten = false;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(NodeContainer node) {
			nodeWriter.process(node.getEntity());
			entitiesWritten = true;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(WayContainer way) {
			wayWriter.process(way.getEntity());
			entitiesWritten = true;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void process(RelationContainer relation) {
			relationWriter.process(relation.getEntity());
			entitiesWritten = true;
		}
		
		
		/**
		 * {@inheritDoc}
		 */
        public void process(BoundContainer bound) {
    		if (boundWritten) {
    			throw new OsmosisRuntimeException("Bound element already written and only one allowed.");
    		}
    		if (entitiesWritten) {
    			throw new OsmosisRuntimeException("Can't write bound element after other entities.");    			
    		}
        	boundWriter.process(bound.getEntity());
    		boundWritten = true;
        }
	}
}
