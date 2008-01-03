// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_5;

import java.util.Comparator;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.sort.common.FileBasedSort;
import com.bretth.osmosis.core.store.GenericObjectSerializationFactory;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkSource;


/**
 * A data stream filter that sorts entities. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class EntitySorter implements SinkSource {
	private FileBasedSort<EntityContainer> fileBasedSort;
	private Sink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public EntitySorter(Comparator<EntityContainer> comparator) {
		fileBasedSort = new FileBasedSort<EntityContainer>(new GenericObjectSerializationFactory(), comparator, true);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		fileBasedSort.add(entityContainer);
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
		ReleasableIterator<EntityContainer> iterator = null;
		
		try {
			iterator = fileBasedSort.iterate();
			
			while (iterator.hasNext()) {
				sink.process(iterator.next());
			}
			
			sink.complete();
		} finally {
			if (iterator != null) {
				iterator.release();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		fileBasedSort.release();
		sink.release();
	}
}
