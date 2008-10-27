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
 * Reads all relation members from a database ordered by the relation
 * identifier.
 * 
 * @author Brett Henderson
 */
public class RelationMemberTableReader extends BaseTableReader<DbFeatureHistory<DbFeature<RelationMember>>> {
	private static final String SELECT_SQL =
		"SELECT id as relation_id, version, member_type, member_id, member_role"
		+ " FROM relation_members"
		+ " ORDER BY id, version";
	
	private MemberTypeParser memberTypeParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public RelationMemberTableReader(DatabaseLoginCredentials loginCredentials) {
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
	protected ReadResult<DbFeatureHistory<DbFeature<RelationMember>>> createNextValue(ResultSet resultSet) {
		long relationId;
		EntityType memberType;
		long memberId;
		String memberRole;
		int version;
		
		try {
			relationId = resultSet.getLong("relation_id");
			memberType = memberTypeParser.parse(resultSet.getString("member_type"));
			memberId = resultSet.getLong("member_id");
			memberRole = resultSet.getString("member_role");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation member fields.", e);
		}
		
		return new ReadResult<DbFeatureHistory<DbFeature<RelationMember>>>(
			true,
			new DbFeatureHistory<DbFeature<RelationMember>>(
				new DbFeature<RelationMember>(
					relationId,
					new RelationMember(
						memberId,
						memberType,
						memberRole
					)
				),
				version
			)
		);
	}
}
