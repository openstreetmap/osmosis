// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao extends EntityDao<Relation> {
	
	private EntityFeatureDao<RelationMember, DbOrderedFeature<RelationMember>> relationMemberDao;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public RelationDao(DatabaseContext dbCtx, ActionDao actionDao) {
		super(dbCtx, new RelationMapper(), actionDao);
		
		relationMemberDao = new EntityFeatureDao<RelationMember, DbOrderedFeature<RelationMember>>(
				dbCtx, new RelationMemberMapper());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadFeatures(long entityId, Relation entity) {
		entity.getMembers().addAll(relationMemberDao.getAllRaw(entityId));
	}
	
	
	/**
	 * Adds the specified relation member list to the database.
	 * 
	 * @param entityId
	 *            The identifier of the entity to add these features to.
	 * @param memberList
	 *            The list of features to add.
	 */
	private void addMembers(long entityId, List<RelationMember> memberList) {
		List<DbOrderedFeature<RelationMember>> dbList;
		
		dbList = new ArrayList<DbOrderedFeature<RelationMember>>(memberList.size());

		for (int i = 0; i < memberList.size(); i++) {
			dbList.add(new DbOrderedFeature<RelationMember>(entityId, memberList.get(i), i));
		}
		
		relationMemberDao.addAll(dbList);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Relation entity) {
		super.addEntity(entity);
		
		addMembers(entity.getId(), entity.getMembers());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Relation entity) {
		long relationId;
		
		super.modifyEntity(entity);
		
		relationId = entity.getId();
		relationMemberDao.removeList(relationId);
		addMembers(entity.getId(), entity.getMembers());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntity(long entityId) {
		relationMemberDao.removeList(entityId);
		
		super.removeEntity(entityId);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Relation> iterate() {
		return new RelationReader(getDatabaseContext());
	}
}
