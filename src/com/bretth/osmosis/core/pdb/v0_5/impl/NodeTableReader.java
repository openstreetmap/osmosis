// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgis.PGgeometry;
import org.postgis.Point;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all nodes from a database ordered by their identifier. These nodes won't
 * be populated with tags.
 * 
 * @author Brett Henderson
 */
public class NodeTableReader extends BaseTableReader<Node> {
	private static final String SELECT_SQL =
		"SELECT id, user_name, tstamp, coordinate"
		+ " FROM node"
		+ " ORDER BY id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public NodeTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeQuery(SELECT_SQL);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<Node> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		PGgeometry coordinate;
		Point coordinatePoint;
		double latitude;
		double longitude;
		
		try {
			id = resultSet.getLong("id");
			userName = resultSet.getString("user_name");
			timestamp = new Date(resultSet.getTimestamp("tstamp").getTime());
			coordinate = (PGgeometry) resultSet.getObject("coordinate");
			coordinatePoint = (Point) coordinate.getGeometry();
			latitude = coordinatePoint.y;
			longitude = coordinatePoint.x;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		return new ReadResult<Node>(
			true,
			new Node(id, timestamp, userName, latitude, longitude)
		);
	}
}
