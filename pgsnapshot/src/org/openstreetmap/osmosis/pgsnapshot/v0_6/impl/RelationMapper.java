// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.springframework.jdbc.core.RowMapper;


/**
 * Reads and writes relation attributes to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class RelationMapper extends EntityMapper<Relation> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return "relation";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActionDataType getEntityType() {
		return ActionDataType.RELATION;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Relation> getEntityClass() {
		return Relation.class;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getTypeSpecificFieldNames() {
		return new String[] {};
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateEntityParameters(Map<String, Object> args, Relation entity) {
		populateCommonEntityParameters(args, entity);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public RowMapper<Relation> getRowMapper() {
		return new RelationRowMapper();
	}
}
