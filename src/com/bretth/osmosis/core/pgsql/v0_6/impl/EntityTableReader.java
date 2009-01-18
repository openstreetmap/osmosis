// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;

import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.EntityBuilder;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads entities from a database ordered by their identifier. These entities
 * won't be populated with tags.
 * 
 * @author Brett Henderson
 * @param <Te>
 *            The entity type to be supported.
 * @param <Tb>
 *            The builder type for the entity.
 */
public class EntityTableReader<Te extends Entity,  Tb extends EntityBuilder<Te>> extends BaseTableReader<Tb> {
	private EntityMapper<Te, Tb> entityMapper;
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param entityBuilder
	 *            The entity builder implementation to utilise.
	 */
	public EntityTableReader(DatabaseContext dbCtx, EntityMapper<Te, Tb> entityBuilder) {
		super(dbCtx);
		
		this.entityMapper = entityBuilder;
		
		sql =
			entityBuilder.getSqlSelect(false, false) +
			" ORDER BY e.id";
	}
	
	
	/**
	 * Creates a new instance with a constrained search.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param entityBuilder
	 *            The entity builder implementation to utilise.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public EntityTableReader(DatabaseContext dbCtx, EntityMapper<Te, Tb> entityBuilder, String constraintTable) {
		super(dbCtx);
		
		this.entityMapper = entityBuilder;
		
		sql =
			entityBuilder.getSqlSelect(false, false) +
			" INNER JOIN " + constraintTable + " c ON e.id = c.id" +
			" ORDER BY e.id";
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
	protected ReadResult<Tb> createNextValue(ResultSet resultSet) {
		Tb entity;
		
		entity = entityMapper.parseRecord(resultSet);
		
		return new ReadResult<Tb>(true, entity);
	}
}
