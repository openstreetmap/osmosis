// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.pdb.common.BaseTableReader;
import com.bretth.osmosis.core.pdb.common.DatabaseContext;


/**
 * Reads all relations from a database ordered by their identifier. These relations won't
 * be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseTableReader<Relation> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public RelationTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT r.id, r.user_name, r.tstamp" +
			" FROM relations r" +
			" ORDER BY r.id";
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
	public RelationTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql =
			"SELECT r.id, r.user_name, r.tstamp" +
			" FROM relations r" +
			" INNER JOIN " + constraintTable + " c ON r.id = c.id" +
			" ORDER BY r.id";
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
	protected ReadResult<Relation> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		
		try {
			id = resultSet.getLong("id");
			userName = resultSet.getString("user_name");
			timestamp = new Date(resultSet.getTimestamp("tstamp").getTime());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation fields.", e);
		}
		
		return new ReadResult<Relation>(
			true,
			new Relation(id, timestamp, userName)
		);
	}
}
