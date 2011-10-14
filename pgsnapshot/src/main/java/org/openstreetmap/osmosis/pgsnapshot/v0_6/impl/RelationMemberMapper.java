// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.Map;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.springframework.jdbc.core.RowMapper;


/**
 * Reads and writes relation members to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class RelationMemberMapper extends EntityFeatureMapper<DbOrderedFeature<RelationMember>> {
	
	private MemberTypeValueMapper memberTypeValueMapper;
	
	
	/**
	 * Creates a new instance.
	 */
	public RelationMemberMapper() {
		memberTypeValueMapper = new MemberTypeValueMapper();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentEntityName() {
		return "relation";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "relation_members";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlSelect(String tablePrefix, boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT relation_id AS entity_id, member_id, member_type, member_role, sequence_id FROM ");
		resultSql.append("relation_members f");
		if (!tablePrefix.isEmpty()) {
			resultSql.append(" INNER JOIN ").append(tablePrefix).append(getParentEntityName())
				.append("s e ON f.").append(getParentEntityName()).append("_id = e.id");
		}
		if (filterByEntityId) {
			resultSql.append(" WHERE entity_id = ?");
		}
		if (orderBy) {
			resultSql.append(getSqlDefaultOrderBy());
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlDefaultOrderBy() {
		return super.getSqlDefaultOrderBy() + ", sequence_id";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlInsert(int rowCount) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("INSERT INTO relation_members (");
		resultSql.append("relation_id, member_id, member_type, member_role, sequence_id) VALUES ");
		for (int row = 0; row < rowCount; row++) {
			if (row > 0) {
				resultSql.append(", ");
			}
			resultSql.append("(:relationId, :memberId, :memberType, :memberRole, :sequenceId)");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlDelete(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("DELETE FROM relation_members");
		if (filterByEntityId) {
			resultSql.append(" WHERE ").append("relation_id = ?");
		}
		
		return resultSql.toString();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateParameters(Map<String, Object> args, DbOrderedFeature<RelationMember> feature) {
		RelationMember relationMember;
		
		relationMember = feature.getFeature();
		
		args.put("relationId", feature.getEntityId());
		args.put("memberId", relationMember.getMemberId());
		args.put("memberType", memberTypeValueMapper.getMemberType(relationMember.getMemberType()));
		args.put("memberRole", relationMember.getMemberRole());
		args.put("sequenceId", feature.getSequenceId());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RowMapper<DbOrderedFeature<RelationMember>> getRowMapper() {
		return new RelationMemberRowMapper();
	}
}
