// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import org.openstreetmap.osmosis.core.store.GenericObjectReader;
import org.openstreetmap.osmosis.core.store.GenericObjectWriter;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a history record for an entity feature.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity feature that this class stores history for.
 */
public class DbFeatureHistory<T extends Storeable> implements Storeable {
	
	private T feature;
	private int version;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param feature
	 *            The contained feature.
	 * @param version
	 *            The version field.
	 */
	public DbFeatureHistory(T feature, int version) {
		this.feature = feature;
		this.version = version;
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
	@SuppressWarnings("unchecked")
	public DbFeatureHistory(StoreReader sr, StoreClassRegister scr) {
		feature = (T) new GenericObjectReader(sr, scr).readObject();
		
		version = sr.readInteger();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		new GenericObjectWriter(sw, scr).writeObject(feature);
		sw.writeInteger(version);
	}
	
	
	/**
	 * Gets the contained feature.
	 * 
	 * @return The feature.
	 */
	public T getFeature() {
		return feature;
	}
	
	
	/**
	 * Gets the version value.
	 * 
	 * @return The version value.
	 */
	public int getVersion() {
		return version;
	}
}
