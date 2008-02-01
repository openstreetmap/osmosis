package com.bretth.osmosis.util.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads all users from the database.
 * 
 * @author Brett Henderson
 */
public class UserTableReader extends BaseTableReader<User> {
	private static final String SELECT_SQL =
		"SELECT u.id, u.display_name, u.data_public"
		+ " FROM users u";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public UserTableReader(DatabaseLoginCredentials loginCredentials) {
		super(loginCredentials);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeStreamingQuery(SELECT_SQL);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BaseTableReader.ReadResult<User> createNextValue(ResultSet resultSet) {
		long id;
		String displayName;
		boolean dataPublic;
		
		try {
			id = resultSet.getLong("id");
			displayName = resultSet.getString("display_name");
			dataPublic = resultSet.getBoolean("data_public");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read user fields.", e);
		}
		
		return new ReadResult<User>(
			true,
			new User(id, displayName, dataPublic)
		);
	}
}
