// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.WayNode;
import org.openstreetmap.osmosis.core.mysql.v0_5.impl.DBWayNode;
import org.openstreetmap.osmosis.core.pgsql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all way nodes from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class WayNodeTableReader extends BaseTableReader<DBWayNode> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public WayNodeTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT wn.way_id, wn.node_id, wn.sequence_id"
			+ " FROM way_nodes wn"
			+ " ORDER BY wn.way_id";
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public WayNodeTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql =
			"SELECT wn.way_id, wn.node_id, wn.sequence_id"
			+ " FROM way_nodes wn"
			+ " INNER JOIN " + constraintTable + " c ON wn.way_id = c.id"
			+ " ORDER BY wn.way_id";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeQuery(sql);
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
			new DBWayNode(wayId, new WayNode(nodeId), sequenceId)
		);
	}
}
