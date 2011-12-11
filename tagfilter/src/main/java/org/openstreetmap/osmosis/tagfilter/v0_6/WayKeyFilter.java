// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * A simple class to filter way entities by their tag keys.
 * Based on work by Brett Henderson, Karl Newman, Christoph Sommer, Aurelien Jacobs
 * 
 * @author Andrew Byrd
 */
public class WayKeyFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedKeys;

	/**
	 * Creates a new instance.
	 *
	 * @param keyList
	 *            Comma-separated list of allowed keys,
	 *            e.g. "highway,place"
	 */
	public WayKeyFilter(String keyList) {

		allowedKeys = new HashSet<String>();
		String[] keys = keyList.split(",");
		for (int i = 0; i < keys.length; i++) {
			allowedKeys.add(keys[i]);
		}

	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
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
		for (Tag tag : way.getTags()) {
			if (allowedKeys.contains(tag.getKey())) {
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
