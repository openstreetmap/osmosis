package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;
import com.bretth.osmosis.data.Way;


/**
 * Reads all ways from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class WayReader extends EntityReader<Way> {
	private static final String SELECT_SQL =
		"SELECT w.id, w.timestamp"
		+ " FROM ways w"
		+ " INNER JOIN"
		+ " ("
		+ "SELECT id, MAX(version) AS version"
		+ " FROM ways"
		+ " WHERE timestamp < ?"
		+ " GROUP BY id"
		+ ") w2 ON w.id = w2.id AND w.version = w2.version"
		+ " WHERE visible = 1"
		+ " ORDER BY id";
	
	private Date snapshotInstant;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 * @param snapshotInstant
	 *            The state of the node table at this point in time will be
	 *            dumped.  This ensures a consistent snapshot.
	 */
	public WayReader(String host, String database, String user, String password, Date snapshotInstant) {
		super(host, database, user, password);
		
		this.snapshotInstant = snapshotInstant;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			PreparedStatement statement;
			
			statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
			statement.setTimestamp(1, new Timestamp(snapshotInstant.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Way createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way fields.", e);
		}
		
		return new Way(id, timestamp);
	}
}
