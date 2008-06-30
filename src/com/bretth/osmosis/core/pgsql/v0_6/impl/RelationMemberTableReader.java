// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBRelationMember;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all relation members from a database ordered by the relation
 * identifier.
 * 
 * @author Brett Henderson
 */
public class RelationMemberTableReader extends BaseTableReader<DBRelationMember> {
	private String sql;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public RelationMemberTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
		
		sql =
			"SELECT rm.relation_id, rm.member_id, rm.member_role, rm.member_type"
			+ " FROM relation_members rm"
			+ " ORDER BY rm.relation_id";
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
	public RelationMemberTableReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx);
		
		sql =
			"SELECT rm.relation_id, rm.member_id, rm.member_role, rm.member_type" +
			" FROM relation_members rm" +
			" INNER JOIN " + constraintTable + " c ON rm.relation_id = c.id" +
			" ORDER BY rm.relation_id";
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
	protected ReadResult<DBRelationMember> createNextValue(ResultSet resultSet) {
		long relationId;
		long memberId;
		EntityType memberType;
		String memberRole;
		
		try {
			relationId = resultSet.getLong("relation_id");
			memberId = resultSet.getLong("member_id");
			memberType = EntityType.values()[resultSet.getByte("member_type")];
			memberRole = resultSet.getString("member_role");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation node fields.", e);
		}
		
		return new ReadResult<DBRelationMember>(
			true,
			new DBRelationMember(relationId, new RelationMember(memberId, memberType, memberRole))
		);
	}
}
