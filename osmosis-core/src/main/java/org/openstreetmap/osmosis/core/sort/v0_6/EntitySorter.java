// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.GenericObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


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
		this(comparator, true);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param comparator
	 *            The comparator to use for sorting.
	 * @param useCompression
	 *            If true, the storage files will be compressed.
	 */
	public EntitySorter(Comparator<EntityContainer> comparator, boolean useCompression) {
		fileBasedSort = new FileBasedSort<EntityContainer>(new GenericObjectSerializationFactory(), comparator, useCompression);
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
		try (ReleasableIterator<EntityContainer> iterator = fileBasedSort.iterate()) {
			while (iterator.hasNext()) {
				sink.process(iterator.next());
			}

			sink.complete();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void close() {
		fileBasedSort.close();
		sink.close();
	}
}
