// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.OsmUser;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Reads complete node history from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class NodeReader extends BaseEntityReader<EntityHistory<Node>> {
	private static final String SELECT_SQL =
		"SELECT n.id, n.timestamp, u.data_public, u.id AS user_id, u.display_name, n.latitude, n.longitude, n.tags, n.visible"
		+ " FROM nodes n"
		+ " LEFT OUTER JOIN users u ON n.user_id = u.id"
		+ " ORDER BY n.id";
	
	private EmbeddedTagProcessor tagParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public NodeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		super(loginCredentials, readAllUsers);
		
		tagParser = new EmbeddedTagProcessor();
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
	protected ReadResult<EntityHistory<Node>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		OsmUser user;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			user = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getInt("user_id"),
				resultSet.getString("display_name")
			);
			latitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("latitude"));
			longitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("longitude"));
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, user, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		return new ReadResult<EntityHistory<Node>>(
			true,
			new EntityHistory<Node>(node, 0, visible)
		);
	}
}
