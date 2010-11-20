// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.ResultSet;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.pgsimple.common.BaseTableReader;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Reads entities from a database ordered by their identifier. These entities
 * won't be populated with tags.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public class EntityTableReader<T extends Entity> extends BaseTableReader<T> {
	private EntityMapper<T> entityMapper;
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param entityBuilder
	 *            The entity builder implementation to utilise.
	 */
	public EntityTableReader(DatabaseContext dbCtx, EntityMapper<T> entityBuilder) {
		super(dbCtx);
		
		this.entityMapper = entityBuilder;
		
		sql =
			entityBuilder.getSqlSelect(false, false)
			+ " ORDER BY e.id";
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
	public EntityTableReader(DatabaseContext dbCtx, EntityMapper<T> entityBuilder, String constraintTable) {
		super(dbCtx);
		
		this.entityMapper = entityBuilder;
		
		sql =
			entityBuilder.getSqlSelect(false, false)
			+ " INNER JOIN " + constraintTable + " c ON e.id = c.id"
			+ " ORDER BY e.id";
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
	protected ReadResult<T> createNextValue(ResultSet resultSet) {
		T entity;
		
		entity = entityMapper.parseRecord(resultSet);
		
		return new ReadResult<T>(true, entity);
	}
}
