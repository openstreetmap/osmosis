// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
 * A simple class to filter way tags
 * 
 * @author Brett Henderson
 * @author Karl Newman
 * @author Christoph Sommer 
 * @author David Turner
 */
public class WayTagFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedTagKeys;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyValueList
	 *            Comma-separated list of allowed key-value combinations,
	 *            e.g. "highway.motorway,highway.motorway_link" 
	 */
	public WayTagFilter(String keyList) {

		allowedTagKeys = new HashSet<String>();
		String[] keys = keyList.split(",");
    allowedTagKeys.addAll(Arrays.asList(keys));
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
    WayContainer writeable = container.getWriteableInstance();
		Way way = writeable.getEntity();

    Collection<Tag> tags = way.getTags();
    ArrayList<Tag> tagsToRemove = new ArrayList<Tag>();
		for (Tag tag : tags) {
        String key = tag.getKey();
        if (!allowedTagKeys.contains(key)) {
            tagsToRemove.add(tag);
        }
		}

    tags.removeAll(tagsToRemove);

    sink.process(writeable);
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
