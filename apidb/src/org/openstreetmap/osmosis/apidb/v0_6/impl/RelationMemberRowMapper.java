// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.springframework.jdbc.core.RowCallbackHandler;


/**
 * Maps relation member result set rows into relation member objects.
 */
public class RelationMemberRowMapper implements RowCallbackHandler {
	
	private RowMapperListener<RelationMember> listener;
	private MemberTypeParser memberTypeParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param listener
	 *            The destination for result objects.
	 */
	public RelationMemberRowMapper(RowMapperListener<RelationMember> listener) {
		this.listener = listener;
		
		memberTypeParser = new MemberTypeParser();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processRow(ResultSet resultSet) throws SQLException {
        long memberId;
        EntityType memberType;
        String memberRole;
        RelationMember relationMember;
        
		memberId = resultSet.getLong("member_id");
		memberType = memberTypeParser.parse(resultSet.getString("member_type"));
		memberRole = resultSet.getString("member_role");
		
		relationMember = new RelationMember(memberId, memberType, memberRole);
		
        listener.process(relationMember, resultSet);
	}
}
