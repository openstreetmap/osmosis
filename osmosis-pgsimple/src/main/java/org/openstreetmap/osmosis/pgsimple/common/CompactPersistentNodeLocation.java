// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.common;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * The compact persistent node location store persists instances of this class.
 * 
 * @author Brett Henderson
 */
public class CompactPersistentNodeLocation implements Storeable {

	private NodeLocation nodeLocation;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeLocation The node location details.
	 */
	public CompactPersistentNodeLocation(NodeLocation nodeLocation) {
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
	public CompactPersistentNodeLocation(StoreReader sr, StoreClassRegister scr) {
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
