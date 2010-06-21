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
public class DbFeatureHistoryRowMapper<T extends Storeable> implements RowMapperListener<T> {
	private RowMapperListener<DbFeatureHistory<T>> listener;


	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public DbFeatureHistoryRowMapper(RowMapperListener<DbFeatureHistory<T>> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(T data, ResultSet resultSet) throws SQLException {
		int version;

		// Get the entity version.
		version = resultSet.getInt("version");

		listener.process(new DbFeatureHistory<T>(data, version), resultSet);
	}
}
