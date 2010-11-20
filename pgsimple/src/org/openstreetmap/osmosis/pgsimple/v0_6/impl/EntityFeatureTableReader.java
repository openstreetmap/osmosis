// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.ResultSet;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.pgsimple.common.BaseTableReader;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Reads all features of a particular type for an entity from a feature table
 * ordered by the entity identifier.
 * 
 * @author Brett Henderson
 * @param <Tef>
 *            The entity feature type to be read.
 * @param <Tdb>
 *            The entity feature database wrapper type to be used.
 */
public class EntityFeatureTableReader<Tef extends Storeable, Tdb extends DbFeature<Tef>> extends BaseTableReader<Tdb> {
	
	private EntityFeatureMapper<Tdb> entityFeatureBuilder;
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param entityFeatureBuilder
	 *            Provides entity feature jdbc bindings.
	 */
	public EntityFeatureTableReader(DatabaseContext dbCtx, EntityFeatureMapper<Tdb> entityFeatureBuilder) {
		super(dbCtx);
		
		this.entityFeatureBuilder = entityFeatureBuilder;
		
		sql = entityFeatureBuilder.getSqlSelect(false, true);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param entityFeatureBuilder
	 *            Provides entity feature jdbc bindings.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public EntityFeatureTableReader(
			DatabaseContext dbCtx, EntityFeatureMapper<Tdb> entityFeatureBuilder, String constraintTable) {
		super(dbCtx);
		
		this.entityFeatureBuilder = entityFeatureBuilder;
		
		sql =
			entityFeatureBuilder.getSqlSelect(false, false)
			+ " INNER JOIN " + constraintTable
			+ " c ON f." + entityFeatureBuilder.getParentEntityName() + "_id = c.id"
			+ entityFeatureBuilder.getSqlDefaultOrderBy();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeQuery(sql);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<Tdb> createNextValue(ResultSet resultSet) {
		return new ReadResult<Tdb>(
			true,
			entityFeatureBuilder.buildEntity(resultSet)
		);
	}
}
