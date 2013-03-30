// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;


/**
 * A change stream filter that sorts changes. The sort order is specified by
 * comparator provided during instantiation.
 * 
 * @author Brett Henderson
 */
public class ChangeSorter implements ChangeSinkChangeSource {
	private FileBasedSort<ChangeContainer> fileBasedSort;
	private ChangeSink changeSink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param comparator
	 *            The comparator to use for sorting.
	 */
	public ChangeSorter(Comparator<ChangeContainer> comparator) {
		fileBasedSort =
			new FileBasedSort<ChangeContainer>(
					new SingleClassObjectSerializationFactory(ChangeContainer.class), comparator, true);
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		changeSink.initialize(metaData);
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
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<ChangeContainer> iterator = null;
		
		try {
			iterator = fileBasedSort.iterate();
			
			while (iterator.hasNext()) {
				changeSink.process(iterator.next());
			}
			
			changeSink.complete();
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
		changeSink.release();
	}
}
