// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.NodeBuilder;
import com.bretth.osmosis.core.domain.v0_6.RelationBuilder;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.WayBuilder;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


/**
 * A data stream filter that sorts tags on entities. This is useful for testing
 * to allow two sets of data to be compared for equality.
 * 
 * @author Brett Henderson
 */
public class TagSorter implements SinkSource, EntityProcessor {
	private Sink sink;
	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;
	private RelationBuilder relationBuilder;
	
	
	public TagSorter() {
		nodeBuilder = new NodeBuilder();
		wayBuilder = new WayBuilder();
		relationBuilder = new RelationBuilder();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}


	/**
	 * Sorts the specified tag list.
	 * 
	 * @param tagList
	 *            The tag list to be sorted.
	 * @return A new list containing the sorted tags.
	 */
	private List<Tag> sortTags(Collection<Tag> tagList) {
		List<Tag> sortedTagList;
		
		sortedTagList = new ArrayList<Tag>(tagList);
		Collections.sort(sortedTagList);
		
		return sortedTagList;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer boundContainer) {
		Bound oldBound;
		Bound newBound;
		
		oldBound = boundContainer.getEntity();
		
		newBound = new Bound(
				oldBound.getRight(), oldBound.getLeft(), oldBound.getTop(), oldBound.getBottom(), oldBound.getOrigin());
		
		sink.process(new BoundContainer(newBound));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer nodeContainer) {
		nodeBuilder.initialize(nodeContainer.getEntity());
		nodeBuilder.setTags(sortTags(nodeBuilder.getTags()));
		
		sink.process(new NodeContainer(nodeBuilder.buildEntity()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer wayContainer) {
		wayBuilder.initialize(wayContainer.getEntity());
		wayBuilder.setTags(sortTags(wayBuilder.getTags()));
		
		sink.process(new WayContainer(wayBuilder.buildEntity()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relationContainer) {
		relationBuilder.initialize(relationContainer.getEntity());
		relationBuilder.setTags(sortTags(relationBuilder.getTags()));
		
		sink.process(new RelationContainer(relationBuilder.buildEntity()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
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
}
