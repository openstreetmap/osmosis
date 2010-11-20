// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;


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
	public String getSqlSelect(boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT relation_id AS entity_id, member_id, member_type, member_role, sequence_id FROM ");
		resultSql.append("relation_members f");
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
			resultSql.append("(?, ?, ?, ?, ?)");
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
	public DbOrderedFeature<RelationMember> buildEntity(ResultSet resultSet) {
		try {
			return new DbOrderedFeature<RelationMember>(
				resultSet.getLong("entity_id"),
				new RelationMember(
					resultSet.getLong("member_id"),
					memberTypeValueMapper.getEntityType(resultSet.getString("member_type")),
					resultSet.getString("member_role")
				),
				resultSet.getInt("sequence_id")
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a relation member from the current recordset row.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(
			PreparedStatement statement, int initialIndex, DbOrderedFeature<RelationMember> entityFeature) {
		try {
			int prmIndex;
			RelationMember relationMember;
			
			relationMember = entityFeature.getFeature();
			
			prmIndex = initialIndex;
			
			statement.setLong(prmIndex++, entityFeature.getEntityId());
			statement.setLong(prmIndex++, relationMember.getMemberId());
			statement.setString(prmIndex++, memberTypeValueMapper.getMemberType(relationMember.getMemberType()));
			statement.setString(prmIndex++, relationMember.getMemberRole());
			statement.setInt(prmIndex++, entityFeature.getSequenceId());
			
			return prmIndex;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to populate relation member parameters for relation "
					+ entityFeature.getEntityId() + ".",
				e
			);
		}
	}
}
