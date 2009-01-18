// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;

import com.bretth.osmosis.core.mysql.v0_6.impl.DbFeature;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.Storeable;


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
	public EntityFeatureTableReader(DatabaseContext dbCtx, EntityFeatureMapper<Tdb> entityFeatureBuilder, String constraintTable) {
		super(dbCtx);
		
		this.entityFeatureBuilder = entityFeatureBuilder;
		
		sql =
			entityFeatureBuilder.getSqlSelect(false, false) +
			" INNER JOIN " + constraintTable + " c ON f." + entityFeatureBuilder.getParentEntityName() + "_id = c.id" +
			entityFeatureBuilder.getSqlDefaultOrderBy();
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
