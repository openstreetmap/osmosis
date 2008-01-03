// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_5;

import java.util.Comparator;

import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.sort.common.FileBasedSort;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;
import com.bretth.osmosis.core.task.v0_5.ChangeSinkChangeSource;


/**
 * A change stream filter that sorts changes. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class ChangeSorter implements ChangeSinkChangeSource {
	private FileBasedSort<ChangeContainer> fileBasedSort;
	private ChangeSink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public ChangeSorter(Comparator<ChangeContainer> comparator) {
		fileBasedSort = new FileBasedSort<ChangeContainer>(new SingleClassObjectSerializationFactory(ChangeContainer.class), comparator, true);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		fileBasedSort.add(change);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<ChangeContainer> iterator = null;
		
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
