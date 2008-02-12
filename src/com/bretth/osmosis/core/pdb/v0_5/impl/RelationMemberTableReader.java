// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBRelationMember;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all relation members from a database ordered by the relation
 * identifier.
 * 
 * @author Brett Henderson
 */
public class RelationMemberTableReader extends BaseTableReader<DBRelationMember> {
	private static final String SELECT_SQL =
		"SELECT relation_id, member_id, member_role, member_type"
		+ " FROM relation_member"
		+ " ORDER BY relation_id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public RelationMemberTableReader(DatabaseContext dbCtx) {
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
