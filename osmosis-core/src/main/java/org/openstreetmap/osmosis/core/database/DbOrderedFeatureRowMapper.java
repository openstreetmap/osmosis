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
public class DbOrderedFeatureRowMapper<T extends Storeable> implements RowMapperListener<DbFeature<T>> {
	private RowMapperListener<DbOrderedFeature<T>> listener;


	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public DbOrderedFeatureRowMapper(RowMapperListener<DbOrderedFeature<T>> listener) {
		this.listener = listener;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(DbFeature<T> data, ResultSet resultSet) throws SQLException {
		int sequence;

		// Get the entity sequence number.
		sequence = resultSet.getInt("sequence_id");

		listener.process(new DbOrderedFeature<T>(data.getEntityId(), data.getFeature(), sequence), resultSet);
	}
}
