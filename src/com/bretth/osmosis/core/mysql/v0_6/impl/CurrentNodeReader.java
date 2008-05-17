// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.mysql.common.BaseEntityReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Reads current nodes from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class CurrentNodeReader extends BaseEntityReader<Node> {
	private static final String SELECT_SQL =
		"SELECT n.id, n.timestamp, u.data_public, u.display_name, n.latitude, n.longitude, n.tags, n.visible"
		+ " FROM current_nodes n"
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
	public CurrentNodeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
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
	protected ReadResult<Node> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		double latitude;
		double longitude;
		String tags;
		boolean visible;
		Node node;
		
		try {
			id = resultSet.getLong("id");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			userName = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getString("display_name")
			);
			latitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("latitude"));
			longitude = FixedPrecisionCoordinateConvertor.convertToDouble(resultSet.getInt("longitude"));
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, userName, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		// Non-visible records will be ignored by the caller.
		return new ReadResult<Node>(
			visible,
			node
		);
	}
}
