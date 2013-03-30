// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.common;

import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;
import org.openstreetmap.osmosis.core.store.NoSuchIndexElementException;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * A file-based node location store implementation. This differs from the normal
 * file-based implementation in that it consumes disk space proportionally to
 * the number of nodes being managed. This is more efficient for smaller data
 * sets, but less efficient when processing a full planet.
 * 
 * @author Brett Henderson
 */
public class CompactPersistentNodeLocationStore implements NodeLocationStore {

	private IndexedObjectStore<CompactPersistentNodeLocation> nodeLocations;
	private IndexedObjectStoreReader<CompactPersistentNodeLocation> nodeLocationsReader;
	
	
	/**
	 * Creates a new instance.
	 */
	public CompactPersistentNodeLocationStore() {
		nodeLocations = new IndexedObjectStore<CompactPersistentNodeLocation>(
				new SingleClassObjectSerializationFactory(CompactPersistentNodeLocation.class),
				"nodeLocation");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLocation(long nodeId, NodeLocation nodeLocation) {
		nodeLocations.add(nodeId, new CompactPersistentNodeLocation(nodeLocation));
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeLocation getNodeLocation(long nodeId) {
		if (nodeLocationsReader == null) {
			nodeLocations.complete();
			nodeLocationsReader = nodeLocations.createReader();
		}
		
		try {
			return nodeLocationsReader.get(nodeId).getNodeLocation();
			
		} catch (NoSuchIndexElementException e) {
			return new NodeLocation();
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (nodeLocationsReader != null) {
			nodeLocationsReader.release();
		}
		
		nodeLocations.release();
	}
}
