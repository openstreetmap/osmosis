// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgis.PGgeometry;
import org.postgis.Point;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.pgsql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads nodes from a database ordered by their identifier. These nodes won't be
 * populated with tags.
 * 
 * @author Brett Henderson
 */
public class NodeTableReader extends BaseTableReader<Node> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public NodeTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT n.id, n.user_id, n.user_name, n.tstamp, n.geom"
			+ " FROM nodes n"
			+ " ORDER BY n.id";
	}
	
	
	/**
	 * Creates a new instance with a constrained search.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public NodeTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql =
			"SELECT n.id, n.user_name, n.tstamp, n.geom"
			+ " FROM nodes n"
			+ " INNER JOIN " + constraintTable + " c ON n.id = c.id"
			+ " ORDER BY n.id";
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
	protected ReadResult<Node> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		OsmUser user;
		PGgeometry coordinate;
		Point coordinatePoint;
		double latitude;
		double longitude;
		
		try {
			id = resultSet.getLong("id");
			if (resultSet.getInt("user_id") != OsmUser.NONE.getId()) {
				user = new OsmUser(resultSet.getInt("user_id"), resultSet.getString("user_name"));
			} else {
				user = OsmUser.NONE;
			}
			timestamp = new Date(resultSet.getTimestamp("tstamp").getTime());
			coordinate = (PGgeometry) resultSet.getObject("geom");
			coordinatePoint = (Point) coordinate.getGeometry();
			latitude = coordinatePoint.y;
			longitude = coordinatePoint.x;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		return new ReadResult<Node>(
			true,
			new Node(id, timestamp, user, latitude, longitude)
		);
	}
}
