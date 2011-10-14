// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.dataset.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.store.IndexStoreReader;
import org.openstreetmap.osmosis.core.store.LongLongIndexElement;
import org.openstreetmap.osmosis.core.store.RandomAccessObjectStoreReader;


/**
 * Holds references to all of the node storage related classes.
 * 
 * @author Brett Henderson
 */
public class RelationStorageContainer implements Releasable {
	private ReleasableContainer releasableContainer;
	private RandomAccessObjectStoreReader<Relation> relationObjectReader;
	private IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader;
	private IndexStoreReader<Long, LongLongIndexElement> relationRelationIndexReader;


	/**
	 * Creates a new instance.
	 * 
	 * @param relationObjectReader
	 *            The raw relation objects.
	 * @param relationObjectOffsetIndexReader
	 *            The relation object offsets.
	 * @param relationRelationIndexReader
	 *            The relation to relation index.
	 */
	public RelationStorageContainer(RandomAccessObjectStoreReader<Relation> relationObjectReader,
			IndexStoreReader<Long, LongLongIndexElement> relationObjectOffsetIndexReader,
			IndexStoreReader<Long, LongLongIndexElement> relationRelationIndexReader) {
		
		releasableContainer = new ReleasableContainer();
		
		this.relationObjectReader = releasableContainer.add(relationObjectReader);
		this.relationObjectOffsetIndexReader = releasableContainer.add(relationObjectOffsetIndexReader);
		this.relationRelationIndexReader = releasableContainer.add(relationRelationIndexReader);
	}


	/**
	 * Gets the raw relation reader.
	 * 
	 * @return The raw relation reader.
	 */
	public RandomAccessObjectStoreReader<Relation> getRelationObjectReader() {
		return relationObjectReader;
	}


	/**
	 * Gets the relation object offset reader.
	 * 
	 * @return The relation object offset reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getRelationObjectOffsetIndexReader() {
		return relationObjectOffsetIndexReader;
	}


	/**
	 * Gets the relation to relation index reader.
	 * 
	 * @return The relation to relation index reader.
	 */
	public IndexStoreReader<Long, LongLongIndexElement> getRelationRelationIndexReader() {
		return relationRelationIndexReader;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		releasableContainer.release();
	}
}
