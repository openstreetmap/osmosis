// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.database.FeaturePopulator;
import org.openstreetmap.osmosis.core.database.RelationMemberCollectionLoader;
import org.openstreetmap.osmosis.core.database.SortingStoreRowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.openstreetmap.osmosis.core.store.UpcastIterator;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.common.RowMapperRowCallbackListener;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao extends EntityDao<Relation> {
	
	private JdbcTemplate jdbcTemplate;
	private EntityFeatureDao<RelationMember, DbOrderedFeature<RelationMember>> relationMemberDao;
	private RelationMemberMapper relationMemberMapper;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public RelationDao(DatabaseContext dbCtx, ActionDao actionDao) {
		super(dbCtx.getJdbcTemplate(), new RelationMapper(), actionDao);
		
		jdbcTemplate = dbCtx.getJdbcTemplate();
		relationMemberMapper = new RelationMemberMapper();
		relationMemberDao = new EntityFeatureDao<RelationMember, DbOrderedFeature<RelationMember>>(
				dbCtx.getJdbcTemplate(), relationMemberMapper);
	}
	
	
	private void loadFeatures(long entityId, Relation entity) {
		entity.getMembers().addAll(relationMemberDao.getAllRaw(entityId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getEntity(long entityId) {
		Relation entity;
		
		entity = super.getEntity(entityId);
		
		loadFeatures(entityId, entity);
		
		return entity;
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
	
	
	private ReleasableIterator<DbOrderedFeature<RelationMember>> getRelationMembers(String tablePrefix) {
		
		FileBasedSort<DbOrderedFeature<RelationMember>> sortingStore =
			new FileBasedSort<DbOrderedFeature<RelationMember>>(
				new SingleClassObjectSerializationFactory(DbOrderedFeature.class),
				new DbOrderedFeatureComparator<RelationMember>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<DbOrderedFeature<RelationMember>> storeListener;
			RowMapperRowCallbackListener<DbOrderedFeature<RelationMember>> rowCallbackListener;
			ReleasableIterator<DbOrderedFeature<RelationMember>> resultIterator;
			
			sql = relationMemberMapper.getSqlSelect(tablePrefix, false, false);
			
			// Sends all received data into the object store.
			storeListener = new SortingStoreRowMapperListener<DbOrderedFeature<RelationMember>>(sortingStore);
			// Converts result set rows into objects and passes them into the store.
			rowCallbackListener = new RowMapperRowCallbackListener<DbOrderedFeature<RelationMember>>(
					relationMemberMapper.getRowMapper(), storeListener);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			jdbcTemplate.query(sql, rowCallbackListener);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator =
				new StoreReleasingIterator<DbOrderedFeature<RelationMember>>(sortingStore.iterate(), sortingStore);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			sortingStore = null;
			
			return resultIterator;
			
		} finally {
			if (sortingStore != null) {
				sortingStore.release();
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeaturePopulator<Relation>> getFeaturePopulators(String tablePrefix) {
		ReleasableIterator<DbFeature<RelationMember>> relationMemberIterator;
		List<FeaturePopulator<Relation>> featurePopulators;
		
		featurePopulators = new ArrayList<FeaturePopulator<Relation>>();
		
		// Get the way nodes for the selected entities.
		relationMemberIterator = new UpcastIterator<DbFeature<RelationMember>, DbOrderedFeature<RelationMember>>(
				getRelationMembers(tablePrefix));
		
		// Wrap the way node source into a feature populator that can attach them to their
		// owning ways.
		featurePopulators.add(
				new FeaturePopulatorImpl<Relation, RelationMember, DbFeature<RelationMember>>(
						relationMemberIterator, new RelationMemberCollectionLoader()));
		
		return featurePopulators;
	}
}
