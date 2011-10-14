// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class for representing a way node database record. This extends a way
 * node with fields relating it to the owning way.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The feature type to be encapsulated.
 */
public class DbOrderedFeature<T extends Storeable> extends DbFeature<T> {
	
	private int sequenceId;


	/**
	 * Creates a new instance.
	 * 
	 * @param entityId
	 *            The owning entity id.
	 * @param feature
	 *            The feature being referenced.
	 * @param sequenceId
	 *            The order of this feature within the entity.
	 */
	public DbOrderedFeature(long entityId, T feature, int sequenceId) {
		super(entityId, feature);
		
		this.sequenceId = sequenceId;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public DbOrderedFeature(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		this.sequenceId = sr.readInteger();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		sw.writeInteger(sequenceId);
	}
	
	
	/**
	 * @return The sequence id.
	 */
	public int getSequenceId() {
		return sequenceId;
	}
}
