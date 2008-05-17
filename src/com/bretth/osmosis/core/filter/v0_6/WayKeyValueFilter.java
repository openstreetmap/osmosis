// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_6;

import java.util.HashSet;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


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
