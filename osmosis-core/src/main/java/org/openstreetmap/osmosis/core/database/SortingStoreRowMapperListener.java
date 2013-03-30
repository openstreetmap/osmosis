// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A row mapper listener that writes all objects into an object sortingStore.
 * 
 * @param <T>
 *            The type of object to be stored.
 */
public class SortingStoreRowMapperListener<T extends Storeable> implements RowMapperListener<T> {
	
	private FileBasedSort<T> sortingStore;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sortingStore
	 *            The sortingStore to receive objects.
	 */
	public SortingStoreRowMapperListener(FileBasedSort<T> sortingStore) {
		this.sortingStore = sortingStore;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(T data, ResultSet resultSet) throws SQLException {
		sortingStore.add(data);
	}
}
