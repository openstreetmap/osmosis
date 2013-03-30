// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * A data stream filter that sorts tags on entities. This is useful for testing
 * to allow two sets of data to be compared for equality.
 * 
 * @author Brett Henderson
 */
public class TagSorter implements SinkSource {
	private Sink sink;


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
		EntityContainer writeableContainer;
		Entity entity;
		Collection<Tag> sortedTags;
		
		writeableContainer = entityContainer.getWriteableInstance();
		
		entity = writeableContainer.getEntity();
		sortedTags = sortTags(entity.getTags());
		entity.getTags().clear();
		entity.getTags().addAll(sortedTags);
		
		sink.process(writeableContainer);
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
