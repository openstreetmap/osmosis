// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityTag;
import com.bretth.osmosis.core.pdb.common.BaseTableReader;
import com.bretth.osmosis.core.pdb.common.DatabaseContext;


/**
 * Reads all tags for an entity from a tag table ordered by the entity
 * identifier. This relies on the fact that all tag tables have an identical
 * layout.
 * 
 * @author Brett Henderson
 */
public class EntityTagTableReader extends BaseTableReader<DBEntityTag> {
	private static final String SELECT_SQL_1 = "SELECT t.";
	private static final String SELECT_SQL_2 = " AS entity_id, t.k, t.v FROM ";
	private static final String SELECT_SQL_3 = " t ";
	private static final String SELECT_SQL_4 = " ORDER BY t.";
	
	
	private String tableName;
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param tableName
	 *            The name of the table to query tag information from.
	 * @param idColumnName
	 *            The name of the column containing the entity id.
	 */
	public EntityTagTableReader(DatabaseContext dbCtx, String tableName, String idColumnName) {
		super(dbCtx);
		
		this.tableName = tableName;
		
		sql = SELECT_SQL_1 + idColumnName + SELECT_SQL_2 + tableName + SELECT_SQL_3 + SELECT_SQL_4 + idColumnName;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 * @param tableName
	 *            The name of the table to query tag information from.
	 * @param idColumnName
	 *            The name of the column containing the entity id.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public EntityTagTableReader(DatabaseContext dbCtx, String tableName, String idColumnName, String constraintTable) {
		super(dbCtx);
		
		this.tableName = tableName;
		
		sql = SELECT_SQL_1 + idColumnName + SELECT_SQL_2 + tableName + SELECT_SQL_3 + "INNER JOIN " + constraintTable + " c ON t." + idColumnName + "=c.id" + SELECT_SQL_4 + idColumnName;
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
	protected ReadResult<DBEntityTag> createNextValue(ResultSet resultSet) {
		long entityId;
		String key;
		String value;
		
		try {
			entityId = resultSet.getLong("entity_id");
			key = resultSet.getString("k");
			value = resultSet.getString("v");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read entity tag fields from table " + tableName + ".", e);
		}
		
		return new ReadResult<DBEntityTag>(
			true,
			new DBEntityTag(entityId, new Tag(key, value))
		);
	}
}
