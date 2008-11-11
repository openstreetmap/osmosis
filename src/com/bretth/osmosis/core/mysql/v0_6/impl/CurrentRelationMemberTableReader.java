// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.EntityType;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.mysql.common.BaseTableReader;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads current relation members from a database ordered by the relation identifier.
 * 
 * @author Brett Henderson
 */
public class CurrentRelationMemberTableReader extends BaseTableReader<DbOrderedFeature<RelationMember>> {
	private static final String SELECT_SQL =
		"SELECT id AS relation_id, member_type, member_id, member_role, sequence_id"
		+ " FROM current_relation_members"
		+ " ORDER BY id";
	
	private MemberTypeParser memberTypeParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public CurrentRelationMemberTableReader(DatabaseLoginCredentials loginCredentials) {
		super(loginCredentials);
		
		memberTypeParser = new MemberTypeParser();
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
	protected ReadResult<DbOrderedFeature<RelationMember>> createNextValue(ResultSet resultSet) {
		long relationId;
		EntityType memberType;
		long memberId;
		String memberRole;
		int sequenceId;
		
		try {
			relationId = resultSet.getLong("relation_id");
			memberType = memberTypeParser.parse(resultSet.getString("member_type"));
			memberId = resultSet.getLong("member_id");
			memberRole = resultSet.getString("member_role");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation member fields.", e);
		}
		
		return new ReadResult<DbOrderedFeature<RelationMember>>(
			true,
			new DbOrderedFeature<RelationMember>(
				relationId,
				new RelationMember(
					memberId,
					memberType,
					memberRole
				),
				sequenceId
			)
		);
	}
}
