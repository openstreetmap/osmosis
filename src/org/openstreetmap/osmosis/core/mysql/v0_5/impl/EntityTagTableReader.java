// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.mysql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads all tags for an entity from a tag table ordered by the entity
 * identifier. This relies on the fact that all tag tables have an identical
 * layout.
 * 
 * @author Brett Henderson
 */
public class EntityTagTableReader extends BaseTableReader<EntityHistory<DBEntityTag>> {
	private static final String SELECT_SQL_1 = "SELECT id as entity_id, version, k, v FROM ";
	private static final String SELECT_SQL_2 = " ORDER BY id, version";
	
	
	private String tableName;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param tableName
	 *            The name of the table to query tag information from.
	 */
	public EntityTagTableReader(DatabaseLoginCredentials loginCredentials, String tableName) {
		super(loginCredentials);
		
		this.tableName = tableName;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeStreamingQuery(SELECT_SQL_1 + tableName + SELECT_SQL_2);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<EntityHistory<DBEntityTag>> createNextValue(ResultSet resultSet) {
		long entityId;
		String key;
		String value;
		int version;
		
		try {
			entityId = resultSet.getLong("entity_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read entity tag fields from table " + tableName + ".", e);
		}
		
		return new ReadResult<EntityHistory<DBEntityTag>>(
			true,
			new EntityHistory<DBEntityTag>(new DBEntityTag(entityId, new Tag(key, value)), version, true)
		);
	}
}
