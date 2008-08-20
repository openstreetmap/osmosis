package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.postgis.PGgeometry;
import org.postgis.Point;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Node;


/**
 * Creates nodes from result set rows.
 * 
 * @author Brett Henderson
 */
public class NodeBuilder extends EntityBuilder<Node> {
	/**
	 * The base SQL SELECT statement for retrieving node details.
	 */
	public static final String SQL_SELECT =
		"SELECT e.id, e.version, e.user_id, u.name AS user_name, e.tstamp, e.geom" +
		" FROM nodes e" +
		" LEFT OUTER JOIN users u ON e.user_id = u.id";
	
	/**
	 * The resultset id field.
	 */
	private static final String FIELD_ID = "id";
	/**
	 * The resultset version field.
	 */
	private static final String FIELD_VERSION = "version";
	/**
	 * The resultset timestamp field.
	 */
	private static final String FIELD_TIMESTAMP = "tstamp";
	/**
	 * The resultset geometry field.
	 */
	private static final String FIELD_GEOMETRY = "geom";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBaseSql() {
		return SQL_SELECT;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node buildEntity(ResultSet resultSet) {
		try {
			PGgeometry geom;
			Point point;
			
			geom = (PGgeometry) resultSet.getObject(FIELD_GEOMETRY);
			point = (Point) geom.getGeometry();
			
			return new Node(
				resultSet.getLong(FIELD_ID),
				resultSet.getInt(FIELD_VERSION),
				new Date(resultSet.getTimestamp(FIELD_TIMESTAMP).getTime()),
				buildUser(resultSet),
				point.y,
				point.x
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a node from the current recordset row.", e);
		}
	}
}
