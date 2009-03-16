// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.mysql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads the most recent set of tags from a database for entities that have been
 * modified within a time interval.
 * 
 * @author Brett Henderson
 */
public class EntityTagHistoryReader extends BaseTableReader<EntityHistory<DBEntityTag>> {
	private static final String SELECT_SQL_1 =
		"SELECT et.id AS entity_id, et.k, et.v, et.version"
		+ " FROM ";
	private static final String SELECT_SQL_2 =
		" et"
		+ " INNER JOIN ("
		+ "   SELECT id, MAX(version) as version"
		+ "   FROM ";
	private static final String SELECT_SQL_3 =
		"   WHERE timestamp > ? AND timestamp <= ?"
		+ "   GROUP BY id"
		+ " ) entityList ON et.id = entityList.id AND et.version = entityList.version";
	
	private String parentTableName;
	private String tagTableName;
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param parentTableName
	 *            The name of the table containing the parent entity.
	 * @param tagTableName
	 *            The name of the table containing the entity tags.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public EntityTagHistoryReader(
			DatabaseLoginCredentials loginCredentials, String parentTableName, String tagTableName, Date intervalBegin,
			Date intervalEnd) {
		super(loginCredentials);
		
		this.parentTableName = parentTableName;
		this.tagTableName = tagTableName;
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			PreparedStatement statement;
			
			statement = queryDbCtx.prepareStatementForStreaming(
				SELECT_SQL_1 + tagTableName + SELECT_SQL_2 + parentTableName + SELECT_SQL_3
			);
			statement.setTimestamp(1, new Timestamp(intervalBegin.getTime()));
			statement.setTimestamp(2, new Timestamp(intervalEnd.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
					"Unable to read entity tag fields from tables "
					+ parentTableName + " and " + tagTableName + ".", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<EntityHistory<DBEntityTag>> createNextValue(ResultSet resultSet) {
		long entityId;
		String key;
		String value;
		int version;
		
		try {
			entityId = resultSet.getLong("entity_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read entity tag fields.", e);
		}
		
		return new ReadResult<EntityHistory<DBEntityTag>>(
			true,
			new EntityHistory<DBEntityTag>(
				new DBEntityTag(entityId, new Tag(key, value)), version, true)
		);
	}
}
