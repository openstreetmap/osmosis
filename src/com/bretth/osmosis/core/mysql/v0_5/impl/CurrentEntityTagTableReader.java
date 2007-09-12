package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;


/**
 * Reads current tags for an entity from a tag table ordered by the entity
 * identifier.  The table must match an expected schema.
 * 
 * @author Brett Henderson
 */
public class CurrentEntityTagTableReader extends BaseTableReader<DBEntityTag> {
	private static final String SELECT_SQL_1 = "SELECT id as way_id, k, v FROM ";
	private static final String SELECT_SQL_2 = " ORDER BY way_id";
	
	
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
	protected ReadResult<DBEntityTag> createNextValue(ResultSet resultSet) {
		long wayId;
		String key;
		String value;
		
		try {
			wayId = resultSet.getLong("way_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way tag fields.", e);
		}
		
		return new ReadResult<DBEntityTag>(
			true,
			new DBEntityTag(wayId, key, value)
		);
	}
}
