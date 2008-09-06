// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads current tags for an entity from a tag table ordered by the entity
 * identifier.   This relies on the fact that all tag tables have an identical
 * layout.
 * 
 * @author Brett Henderson
 */
public class CurrentEntityTagTableReader extends BaseTableReader<DBEntityFeature<Tag>> {
	private static final String SELECT_SQL_1 = "SELECT id as entity_id, k, v FROM ";
	private static final String SELECT_SQL_2 = " ORDER BY id";
	
	
	private String tableName;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param tableName
	 *            The name of the table to query tag information from.
	 */
	public CurrentEntityTagTableReader(DatabaseLoginCredentials loginCredentials, String tableName) {
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
	protected ReadResult<DBEntityFeature<Tag>> createNextValue(ResultSet resultSet) {
		long entityId;
		String key;
		String value;
		
		try {
			entityId = resultSet.getLong("entity_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read entity tag fields from table " + tableName + ".", e);
		}
		
		return new ReadResult<DBEntityFeature<Tag>>(
			true,
			new DBEntityFeature<Tag>(entityId, new Tag(key, value))
		);
	}
}
