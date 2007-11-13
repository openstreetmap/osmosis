package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.mysql.common.BaseEntityReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.mysql.common.FixedPrecisionCoordinateConvertor;


/**
 * Reads node history records for nodes that have been modified within a time
 * interval. All history items will be returned for the node from node creation
 * up to the end of the time interval. We need the complete history instead of
 * just the history within the interval so we can determine if the node was
 * created during the interval or prior to the interval, a version attribute
 * would eliminate the need for full history.
 * 
 * @author Brett Henderson
 */
public class NodeHistoryReader extends BaseEntityReader<EntityHistory<Node>> {
	// The sub-select identifies the nodes that have been modified within the
	// time interval. The outer query then queries all node history items up to
	// the end of the time interval.
	private static final String SELECT_SQL =
		"SELECT n.id, n.timestamp, u.data_public, u.display_name, n.latitude, n.longitude, n.tags, n.visible" +
		" FROM nodes n" +
		" INNER JOIN (" +
		"   SELECT id" +
		"   FROM nodes" +
		"   WHERE timestamp > ? AND timestamp <= ?" +
		"   GROUP BY id" +
		" ) idList ON n.id = idList.id" +
		" LEFT OUTER JOIN users u ON n.user_id = u.id" +
		" WHERE n.timestamp <= ?" +
		" ORDER BY n.id, n.timestamp";
	
	
	private EmbeddedTagProcessor tagParser;
	private FixedPrecisionCoordinateConvertor fixedPrecisionConvertor;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public NodeHistoryReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		super(loginCredentials, readAllUsers);
		
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
		
		tagParser = new EmbeddedTagProcessor();
		fixedPrecisionConvertor = new FixedPrecisionCoordinateConvertor();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			PreparedStatement statement;
			
			statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
			
			statement.setTimestamp(1, new Timestamp(intervalBegin.getTime()));
			statement.setTimestamp(2, new Timestamp(intervalEnd.getTime()));
			statement.setTimestamp(3, new Timestamp(intervalEnd.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
		}
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
		EntityHistory<Node> nodeHistory;
		
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
		
		nodeHistory = new EntityHistory<Node>(node, 0, visible);
		
		return new ReadResult<EntityHistory<Node>>(true, nodeHistory);
	}
}
