// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * Wraps database features within a database feature object containing the owning entity id.
 * 
 * @param <T>
 *            The type of feature to be wrapped.
 */
public class DbFeatureRowMapper<T extends Storeable> implements RowMapperListener<T> {
	private RowMapperListener<DbFeature<T>> listener;


	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public DbFeatureRowMapper(RowMapperListener<DbFeature<T>> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(T data, ResultSet resultSet) throws SQLException {
		long id;

		// Get the owning entity id.
		id = resultSet.getLong("id");

		listener.process(new DbFeature<T>(id, data), resultSet);
	}
}
