package com.bretth.osmosis.sort;

import java.util.Comparator;

import com.bretth.osmosis.container.EntityContainer;
import com.bretth.osmosis.sort.impl.FileBasedSort;
import com.bretth.osmosis.sort.impl.ReleasableIterator;
import com.bretth.osmosis.task.Sink;
import com.bretth.osmosis.task.SinkSource;


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
		fileBasedSort = new FileBasedSort<EntityContainer>(comparator, true);
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
