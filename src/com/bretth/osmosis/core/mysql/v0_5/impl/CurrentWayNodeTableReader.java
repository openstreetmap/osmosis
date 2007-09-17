package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.DatabaseLoginCredentials;


/**
 * Reads current way nodes from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class CurrentWayNodeTableReader extends BaseTableReader<DBWayNode> {
	private static final String SELECT_SQL =
		"SELECT id as way_id, node_id, sequence_id"
		+ " FROM current_way_nodes"
		+ " ORDER BY id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public CurrentWayNodeTableReader(DatabaseLoginCredentials loginCredentials) {
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
	protected ReadResult<DBWayNode> createNextValue(ResultSet resultSet) {
		long wayId;
		long nodeId;
		int sequenceId;
		
		try {
			wayId = resultSet.getLong("way_id");
			nodeId = resultSet.getLong("node_id");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way node fields.", e);
		}
		
		return new ReadResult<DBWayNode>(
			true,
			new DBWayNode(wayId, nodeId, sequenceId)
		);
	}
}
