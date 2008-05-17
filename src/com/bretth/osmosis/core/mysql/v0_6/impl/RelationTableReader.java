// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.mysql.common.BaseEntityReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;


/**
 * Reads all relations from a database ordered by their identifier. These relations won't
 * be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseEntityReader<EntityHistory<Relation>> {
	private static final String SELECT_SQL =
		"SELECT r.id, r.version, r.timestamp, u.data_public, u.display_name, r.visible"
		+ " FROM relations r"
		+ " LEFT OUTER JOIN users u ON r.user_id = u.id"
		+ " ORDER BY r.id, r.version";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public RelationTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		super(loginCredentials, readAllUsers);
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
	protected ReadResult<EntityHistory<Relation>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		int version;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			version = resultSet.getInt("version");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			userName = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getString("display_name")
			);
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation fields.", e);
		}
		
		return new ReadResult<EntityHistory<Relation>>(
			true,
			new EntityHistory<Relation>(new Relation(id, timestamp, userName), version, visible)
		);
	}
}
