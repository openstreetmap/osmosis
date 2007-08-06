package com.bretth.osmosis.mysql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.OsmosisRuntimeException;


/**
 * Reads all way segments from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class WaySegmentReader extends EntityReader<WaySegment> {
	private static final String SELECT_SQL =
		"SELECT ws.id as way_id, ws.segment_id, ws.sequence_id"
		+ " FROM way_segments ws"
		+ " INNER JOIN"
		+ " ("
		+ "SELECT id, MAX(version) AS version"
		+ " FROM ways"
		+ " WHERE timestamp < ?"
		+ " GROUP BY id"
		+ " ORDER BY id"
		+ ") w ON ws.id = w.id AND ws.version = w.version";
	
	private Date snapshotInstant;
	private long previousWayId;
	
	
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
	public WaySegmentReader(String host, String database, String user, String password, Date snapshotInstant) {
		super(host, database, user, password);
		
		this.snapshotInstant = snapshotInstant;
		
		previousWayId = 0;
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
	protected WaySegment createNextValue(ResultSet resultSet) {
		long wayId;
		long segmentId;
		int sequenceId;
		
		try {
			wayId = resultSet.getLong("way_id");
			segmentId = resultSet.getLong("segment_id");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way segment fields.", e);
		}
		
		if (wayId < previousWayId) {
			throw new OsmosisRuntimeException(
					"Way id of " + wayId + " must be greater or equal to previous way id of " + previousWayId + ".");
		}
		previousWayId = wayId;
		
		return new WaySegment(wayId, segmentId, sequenceId);
	}
}
