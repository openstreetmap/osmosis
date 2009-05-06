// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads all relations from a database ordered by their identifier. These relations won't
 * be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseEntityReader<EntityHistory<Relation>> {
	private static final String SELECT_SQL =
		"SELECT r.id, r.version, r.timestamp, u.data_public, u.id AS user_id, u.display_name, r.visible"
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
		OsmUser user;
		int version;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			version = resultSet.getInt("version");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			user = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getInt("user_id"),
				resultSet.getString("display_name")
			);
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation fields.", e);
		}
		
		return new ReadResult<EntityHistory<Relation>>(
			true,
			new EntityHistory<Relation>(new Relation(id, timestamp, user), version, visible)
		);
	}
}
