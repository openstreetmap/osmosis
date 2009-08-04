// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A row mapper listener that writes all objects into an object store.
 * 
 * @param <T>
 *            The type of object to be stored.
 */
public class ObjectStoreRowMapperListener<T extends Storeable> implements RowMapperListener<T> {
	
	private SimpleObjectStore<T> store;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param store
	 *            The store to receive objects.
	 */
	public ObjectStoreRowMapperListener(SimpleObjectStore<T> store) {
		this.store = store;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(T data, ResultSet resultSet) throws SQLException {
		store.add(data);
	}
}
