// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.mysql.v0_6.impl.DbFeature;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;


/**
 * Reads all relations from a database ordered by their identifier. It combines the
 * output of the relation table readers to produce fully configured relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationReader  extends EntityReader<Relation> {
	
	private PeekableIterator<DbFeature<RelationMember>> relationMemberReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public RelationReader(DatabaseContext dbCtx) {
		super(dbCtx, new RelationBuilder());
		
		relationMemberReader = new PeekableIterator<DbFeature<RelationMember>>(
			new EntityFeatureTableReader<RelationMember, DbFeature<RelationMember>>(dbCtx, new RelationMemberBuilder())
		);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public RelationReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx, new RelationBuilder(), constraintTable);
		
		relationMemberReader = new PeekableIterator<DbFeature<RelationMember>>(
			new EntityFeatureTableReader<RelationMember, DbFeature<RelationMember>>(dbCtx, new RelationMemberBuilder(), constraintTable)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateEntityFeatures(Relation entity) {
		long relationId;
		List<DbFeature<RelationMember>> relationMembers;
		
		super.populateEntityFeatures(entity);
		
		relationId = entity.getId();
		
		// Skip all relation members that are from a lower relation.
		while (relationMemberReader.hasNext()) {
			DbFeature<RelationMember> wayNode;
			
			wayNode = relationMemberReader.peekNext();
			
			if (wayNode.getEntityId() < relationId) {
				relationMemberReader.next();
			} else {
				break;
			}
		}
		
		// Load all nodes matching this version of the way.
		relationMembers = new ArrayList<DbFeature<RelationMember>>();
		while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getEntityId() == relationId) {
			relationMembers.add(relationMemberReader.next());
		}
		for (DbFeature<RelationMember> dbRelationMember : relationMembers) {
			entity.addMember(dbRelationMember.getFeature());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		super.release();
		
		relationMemberReader.release();
	}
}
