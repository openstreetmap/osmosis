// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.tagremove.v0_6;

import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


/**
 * Filters a set of tags from all entities. This allows unwanted tags to be
 * removed from the data.
 * 
 * @author Jochen Topf
 * @author Brett Henderson
 */
public class TagRemover implements SinkSource {
	private TagRemoverBuilder dropTagsBuilder;
	private Sink sink;
	
	
	/**
	 * Creates a new instance.
	 *
	 * @param keyValueList
	 *            Comma-separated list of allowed key-value combinations,
	 *            e.g. "place.city,place.town"
	 */
	public TagRemover(String keyList, String keyPrefixList) {
		dropTagsBuilder = new TagRemoverBuilder(keyList, keyPrefixList);
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
		dropTagsBuilder.setSink(sink);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		entityContainer.process(dropTagsBuilder);
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
