// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.springframework.jdbc.core.RowMapper;


/**
 * Maps database rows into relation member database objects.
 * 
 * @author Brett Henderson
 */
public class RelationMemberRowMapper implements RowMapper<DbOrderedFeature<RelationMember>> {
	
	private MemberTypeValueMapper memberTypeValueMapper;
	
	
	/**
	 * Creates a new instance.
	 */
	public RelationMemberRowMapper() {
		memberTypeValueMapper = new MemberTypeValueMapper();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DbOrderedFeature<RelationMember> mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return new DbOrderedFeature<RelationMember>(
			rs.getLong("entity_id"),
			new RelationMember(
				rs.getLong("member_id"),
				memberTypeValueMapper.getEntityType(rs.getString("member_type")),
				rs.getString("member_role")
			),
			rs.getInt("sequence_id")
		);
	}
}
