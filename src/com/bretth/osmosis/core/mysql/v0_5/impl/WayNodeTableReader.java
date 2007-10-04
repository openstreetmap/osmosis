package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;


/**
 * Reads all way nodes from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class WayNodeTableReader extends BaseTableReader<EntityHistory<DBWayNode>> {
	private static final String SELECT_SQL =
		"SELECT id as way_id, version, node_id, sequence_id"
		+ " FROM way_nodes"
		+ " ORDER BY id, version";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public WayNodeTableReader(DatabaseLoginCredentials loginCredentials) {
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
	protected ReadResult<EntityHistory<DBWayNode>> createNextValue(ResultSet resultSet) {
		long wayId;
		long nodeId;
		int sequenceId;
		int version;
		
		try {
			wayId = resultSet.getLong("way_id");
			nodeId = resultSet.getLong("node_id");
			sequenceId = resultSet.getInt("sequence_id");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way node fields.", e);
		}
		
		return new ReadResult<EntityHistory<DBWayNode>>(
			true,
			new EntityHistory<DBWayNode>(new DBWayNode(wayId, nodeId, sequenceId), version, true)
		);
	}
}
