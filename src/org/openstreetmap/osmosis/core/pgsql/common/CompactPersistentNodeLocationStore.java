package org.openstreetmap.osmosis.core.pgsql.common;

import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A file-based node location store implementation. This differs from the normal
 * file-based implementation in that it consumes disk space proportionally to
 * the number of nodes being managed. This is more efficient for smaller data
 * sets, but less efficient when processing a full planet.
 * 
 * @author Brett Henderson
 */
public class CompactPersistentNodeLocationStore implements NodeLocationStore {

	private IndexedObjectStore<PersistentNodeLocation> nodeLocations;
	private IndexedObjectStoreReader<PersistentNodeLocation> nodeLocationsReader;
	
	
	/**
	 * Creates a new instance.
	 */
	public CompactPersistentNodeLocationStore() {
		nodeLocations = new IndexedObjectStore<PersistentNodeLocation>(
				new SingleClassObjectSerializationFactory(PersistentNodeLocation.class),
				"nodeLocation");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addLocation(long nodeId, NodeLocation nodeLocation) {
		nodeLocations.add(nodeId, new PersistentNodeLocation(nodeLocation));
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeLocation getNodeLocation(long nodeId) {
		PersistentNodeLocation persistentNodeLocation;
		
		if (nodeLocationsReader == null) {
			nodeLocationsReader = nodeLocations.createReader();
		}
		
		persistentNodeLocation = nodeLocationsReader.get(nodeId);
		
		return persistentNodeLocation.getNodeLocation();
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
	
	
	private static class PersistentNodeLocation implements Storeable {

		private NodeLocation nodeLocation;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param nodeLocation The node location details.
		 */
		public PersistentNodeLocation(NodeLocation nodeLocation) {
			this.nodeLocation = nodeLocation;
		}


		/**
		 * Creates a new instance.
		 * 
		 * @param sr
		 *            The store to read state from.
		 * @param scr
		 *            Maintains the mapping between classes and their identifiers within the store.
		 */
		@SuppressWarnings("unused") // Used by the persistent store via reflection.
		public PersistentNodeLocation(StoreReader sr, StoreClassRegister scr) {
			nodeLocation = new NodeLocation(sr.readDouble(), sr.readDouble());
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public void store(StoreWriter writer,
				StoreClassRegister storeClassRegister) {
			writer.writeDouble(nodeLocation.getLongitude());
			writer.writeDouble(nodeLocation.getLatitude());
		}
		
		
		/**
		 * Gets the node location details.
		 * @return The node location.
		 */
		public NodeLocation getNodeLocation() {
			return nodeLocation;
		}
	}
}
