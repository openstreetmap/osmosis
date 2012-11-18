// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.database.FeaturePopulator;
import org.openstreetmap.osmosis.core.database.SortingStoreRowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntitySubClassComparator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.openstreetmap.osmosis.pgsnapshot.common.NoSuchRecordException;
import org.openstreetmap.osmosis.pgsnapshot.common.RowMapperRowCallbackListener;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * Provides functionality common to all top level entity daos.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityDao<T extends Entity> {
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private ActionDao actionDao;
	private EntityMapper<T> entityMapper;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Provides access to the database.
	 * @param entityMapper
	 *            Provides entity type specific JDBC support.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	protected EntityDao(JdbcTemplate jdbcTemplate, EntityMapper<T> entityMapper, ActionDao actionDao) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		this.entityMapper = entityMapper;
		this.actionDao = actionDao;
	}
	
	
	/**
	 * Gets the entity mapper implementation.
	 * 
	 * @return The entity mapper.
	 */
	protected EntityMapper<T> getEntityMapper() {
		return entityMapper;
	}
	
	
	/**
	 * Checks if the specified entity exists in the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return True if the entity exists in the database.
	 */
	public boolean exists(long entityId) {
		return jdbcTemplate.queryForInt(entityMapper.getSqlSelectCount(true), entityId) > 0;
	}
	
	
	/**
	 * Loads the specified entity from the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return The loaded entity.
	 */
	public T getEntity(long entityId) {
		T entity;
		
		try {
			entity = jdbcTemplate.queryForObject(entityMapper.getSqlSelect(true, false), entityMapper.getRowMapper(),
					entityId);
		} catch (EmptyResultDataAccessException e) {
			throw new NoSuchRecordException(entityMapper.getEntityName()
					+ " " + entityId + " doesn't exist.", e);
		}
		
		return entity;
	}
	
	
	/**
	 * Adds the specified entity to the database.
	 * 
	 * @param entity
	 *            The entity to add.
	 */
	public void addEntity(T entity) {
		Map<String, Object> args;
		
		args = new HashMap<String, Object>();
		entityMapper.populateEntityParameters(args, entity);
		
		namedParameterJdbcTemplate.update(entityMapper.getSqlInsert(1), args);
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.CREATE, entity.getId());
	}
	
	
	/**
	 * Updates the specified entity details in the database.
	 * 
	 * @param entity
	 *            The entity to update.
	 */
	public void modifyEntity(T entity) {
		Map<String, Object> args;
		
		args = new HashMap<String, Object>();
		entityMapper.populateEntityParameters(args, entity);
		
		namedParameterJdbcTemplate.update(entityMapper.getSqlUpdate(true), args);
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.MODIFY, entity.getId());
	}
	
	
	/**
	 * Removes the specified entity from the database.
	 * 
	 * @param entityId
	 *            The id of the entity to remove.
	 */
	public void removeEntity(long entityId) {
		Map<String, Object> args;
		
		args = new HashMap<String, Object>();
		args.put("id", entityId);
		
		namedParameterJdbcTemplate.update(entityMapper.getSqlDelete(true), args);
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.DELETE, entityId);
	}
	
	
	private ReleasableIterator<T> getFeaturelessEntity(String tablePrefix) {
		FileBasedSort<T> sortingStore;
		
		sortingStore =
			new FileBasedSort<T>(
				new SingleClassObjectSerializationFactory(entityMapper.getEntityClass()),
				new EntitySubClassComparator<T>(new EntityByTypeThenIdComparator()), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<T> storeListener;
			RowMapperRowCallbackListener<T> rowCallbackListener;
			ReleasableIterator<T> resultIterator;
			
			sql = entityMapper.getSqlSelect(tablePrefix, false, false);
			
			// Sends all received data into the object store.
			storeListener = new SortingStoreRowMapperListener<T>(sortingStore);
			// Converts result set rows into objects and passes them into the store.
			rowCallbackListener = new RowMapperRowCallbackListener<T>(entityMapper.getRowMapper(), storeListener);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			jdbcTemplate.query(sql, rowCallbackListener);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<T>(sortingStore.iterate(), sortingStore);
			
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
	 * Gets the feature populators for the entity type.
	 * 
	 * @param tablePrefix
	 *            The prefix for the entity table name. This allows another table to be queried if
	 *            necessary such as a temporary results table.
	 * @return The feature populators.
	 */
	protected abstract List<FeaturePopulator<T>> getFeaturePopulators(String tablePrefix);
	
	
	/**
	 * Returns an iterator providing access to all entities in the database.
	 * 
	 * @param tablePrefix
	 *            The prefix for the entity table name. This allows another table to be queried if
	 *            necessary such as a temporary results table.
	 * @return The entity iterator.
	 */
	public ReleasableIterator<T> iterate(String tablePrefix) {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		
		try {
			ReleasableIterator<T> entityIterator;
			List<FeaturePopulator<T>> featurePopulators;
			
			// Create the featureless entity iterator but also store it temporarily in the
			// releasable container so that it will get freed if we fail during retrieval of feature
			// populators.
			entityIterator = releasableContainer.add(getFeaturelessEntity(tablePrefix));
			
			// Retrieve the feature populators also adding them to the temporary releasable container.
			featurePopulators = getFeaturePopulators(tablePrefix);
			for (FeaturePopulator<T> featurePopulator : featurePopulators) {
				releasableContainer.add(featurePopulator);
			}
			
			// Build an entity reader capable of merging all sources together.
			entityIterator = new EntityReader<T>(entityIterator, featurePopulators);
			
			// The sources are now all attached to the history reader so we don't want to release
			// them in the finally block.
			releasableContainer.clear();
			
			return entityIterator;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * Returns an iterator providing access to all entities in the database.
	 * 
	 * @return The entity iterator.
	 */
	public ReleasableIterator<T> iterate() {
		return iterate("");
	}
}
