package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.mysql.common.BaseEntityReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.mysql.common.FixedPrecisionCoordinateConvertor;


/**
 * Reads complete node history from a database ordered by their identifier.
 * 
 * @author Brett Henderson
 */
public class NodeReader extends BaseEntityReader<EntityHistory<Node>> {
	private static final String SELECT_SQL =
		"SELECT n.id, n.timestamp, u.data_public, u.display_name, n.latitude, n.longitude, n.tags, n.visible"
		+ " FROM nodes n"
		+ " LEFT OUTER JOIN users u ON n.user_id = u.id"
		+ " ORDER BY n.id";
	
	private EmbeddedTagProcessor tagParser;
	private FixedPrecisionCoordinateConvertor fixedPrecisionConvertor;
	
	
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
		fixedPrecisionConvertor = new FixedPrecisionCoordinateConvertor();
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
			latitude = fixedPrecisionConvertor.convertToDouble(resultSet.getInt("latitude"));
			longitude = fixedPrecisionConvertor.convertToDouble(resultSet.getInt("longitude"));
			tags = resultSet.getString("tags");
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read node fields.", e);
		}
		
		node = new Node(id, timestamp, userName, latitude, longitude);
		node.addTags(tagParser.parseTags(tags));
		
		return new ReadResult<EntityHistory<Node>>(
			true,
			new EntityHistory<Node>(node, 0, visible)
		);
	}
}
