// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Reads all relations from a database ordered by their identifier. It combines the
 * output of the relation table readers to produce fully configured relation objects.
 * 
 * @author Brett Henderson
 */
public class RelationReader  extends EntityReader<Relation> {
	
	private PeekableIterator<DbOrderedFeature<RelationMember>> relationMemberReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public RelationReader(DatabaseContext dbCtx) {
		super(dbCtx, new RelationMapper());
		
		relationMemberReader = new PeekableIterator<DbOrderedFeature<RelationMember>>(
			new EntityFeatureTableReader<RelationMember, DbOrderedFeature<RelationMember>>(
					dbCtx, new RelationMemberMapper())
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
		super(dbCtx, new RelationMapper(), constraintTable);
		
		relationMemberReader = new PeekableIterator<DbOrderedFeature<RelationMember>>(
			new EntityFeatureTableReader<RelationMember, DbOrderedFeature<RelationMember>>(
					dbCtx, new RelationMemberMapper(), constraintTable)
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
		
		// Load all members matching this version of the relation.
		relationMembers = new ArrayList<DbFeature<RelationMember>>();
		while (relationMemberReader.hasNext() && relationMemberReader.peekNext().getEntityId() == relationId) {
			relationMembers.add(relationMemberReader.next());
		}
		for (DbFeature<RelationMember> dbRelationMember : relationMembers) {
			entity.getMembers().add(dbRelationMember.getFeature());
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
