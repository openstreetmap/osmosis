package com.bretth.osmosis.sort;

import java.util.Comparator;

import com.bretth.osmosis.container.ElementContainer;
import com.bretth.osmosis.sort.impl.FileBasedSort;
import com.bretth.osmosis.sort.impl.ReleasableIterator;
import com.bretth.osmosis.task.Sink;
import com.bretth.osmosis.task.SinkSource;


/**
 * A data stream filter that sorts elements. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class ElementSorter implements SinkSource {
	private FileBasedSort<ElementContainer> fileBasedSort;
	private Sink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public ElementSorter(Comparator<ElementContainer> comparator) {
		fileBasedSort = new FileBasedSort<ElementContainer>(comparator, true);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ElementContainer element) {
		fileBasedSort.add(element);
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
		ReleasableIterator<ElementContainer> iterator = null;
		
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
