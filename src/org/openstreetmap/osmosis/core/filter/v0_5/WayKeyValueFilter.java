// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.v0_5;

import java.util.HashSet;

import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;
import org.openstreetmap.osmosis.core.task.v0_5.SinkSource;


/**
 * A base class for all tasks filter entities within an area.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 * @author Christoph Sommer 
 */
public class WayKeyValueFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedKeyValues;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyValueList
	 *            Comma-separated list of allowed key-value combinations,
	 *            e.g. "highway.motorway,highway.motorway_link" 
	 */
	public WayKeyValueFilter(String keyValueList) {

		allowedKeyValues = new HashSet<String>();
		String[] keyValues = keyValueList.split(",");
		for (int i = 0; i < keyValues.length; i++) {
			allowedKeyValues.add(keyValues[i]);
		}

	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		sink.process(container);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way = container.getEntity();

		boolean matchesFilter = false;
		for (Tag tag : way.getTagList()) {
			String keyValue = tag.getKey() + "." + tag.getValue();
			if (allowedKeyValues.contains(keyValue)) {
				matchesFilter = true;
				break;
			}
		}

		if (matchesFilter) {
			sink.process(container);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		sink.process(container);
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
