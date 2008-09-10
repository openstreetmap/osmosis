// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityFeature;
import com.bretth.osmosis.core.pgsql.common.BaseDao;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.NoSuchRecordException;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Provides functionality common to all top level entity daos.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityDao<T extends Entity> extends BaseDao {
	
	private EntityFeatureDao<Tag, DBEntityFeature<Tag>> tagDao;
	private EntityBuilder<T> entityBuilder;
	private PreparedStatement countStatement;
	private PreparedStatement getStatement;
	private PreparedStatement insertStatement;
	private PreparedStatement updateStatement;
	private PreparedStatement deleteStatement;
	private PreparedStatement purgeStatement;
	private PreparedStatement resetStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityBuilder
	 *            Provides entity type specific JDBC support.
	 */
	protected EntityDao(DatabaseContext dbCtx, EntityBuilder<T> entityBuilder) {
		super(dbCtx);
		
		this.entityBuilder = entityBuilder;
		
		tagDao = new EntityFeatureDao<Tag, DBEntityFeature<Tag>>(dbCtx, new TagBuilder(entityBuilder.getEntityName()));
	}
	
	
	/**
	 * Checks if the specified entity exists in the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return True if the entity exists in the database.
	 */
	public boolean exists(long entityId) {
		ResultSet resultSet = null;
		
		if (countStatement == null) {
			countStatement = prepareStatement(entityBuilder.getSqlSelectCount(true));
		}
		
		try {
			boolean result;
			
			countStatement.setLong(1, entityId);
			
			resultSet = countStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new OsmosisRuntimeException(
						"Entity count query didn't return any rows.");
			}
			result = resultSet.getLong("count") > 0;
			
			resultSet.close();
			resultSet = null;
			
			return result;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Count query failed for " +
				entityBuilder.getEntityName() + " " + entityId + ".",
				e
			);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * Loads the specified entity from the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return The loaded entity.
	 */
	public T getEntity(long entityId) {
		ResultSet resultSet = null;
		T entity;
		
		if (getStatement == null) {
			getStatement = prepareStatement(entityBuilder.getSqlSelect(true, true));
		}
		
		try {
			getStatement.setLong(1, entityId);
			
			resultSet = getStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new NoSuchRecordException(entityBuilder.getEntityName()
						+ " " + entityId + " doesn't exist.");
			}
			entity = entityBuilder.buildEntity(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			for (DBEntityFeature<Tag> dbTag : tagDao.getList(entityId)) {
				entity.addTag(dbTag.getEntityFeature());
			}
			
			return entity;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Query failed for " +
				entityBuilder.getEntityName() + " " + entityId + ".",
				e
			);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * Adds the specified tag list to the database.
	 * 
	 * @param entityId
	 *            The identifier of the entity to add these features to.
	 * @param tagList
	 *            The list of features to add.
	 */
	private void addTagList(long entityId, List<Tag> tagList) {
		List<DBEntityFeature<Tag>> dbList;
		
		dbList = new ArrayList<DBEntityFeature<Tag>>(tagList.size());
		
		for (Tag tag : tagList) {
			dbList.add(new DBEntityFeature<Tag>(entityId, tag));
		}
		
		tagDao.addList(dbList);
	}
	
	
	/**
	 * Adds the specified entity to the database.
	 * 
	 * @param entity
	 *            The entity to add.
	 */
	public void addEntity(T entity) {
		if (insertStatement == null) {
			insertStatement = prepareStatement(entityBuilder.getSqlInsert(1));
		}
		
		try {
			entityBuilder.populateEntityParameters(insertStatement, 1, entity);
			insertStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Insert failed for " + entityBuilder.getEntityName() +
				" " + entity.getId() + ".",
				e
			);
		}
		
		addTagList(entity.getId(), entity.getTagList());
	}
	
	
	/**
	 * Updates the specified entity details in the database.
	 * 
	 * @param entity
	 *            The entity to update.
	 */
	public void modifyEntity(T entity) {
		if (updateStatement == null) {
			updateStatement = prepareStatement(entityBuilder.getSqlUpdate(true));
		}
		
		try {
			int prmIndex;
			
			prmIndex = 1;
			
			prmIndex = entityBuilder.populateEntityParameters(updateStatement, prmIndex, entity);
			updateStatement.setLong(prmIndex++, entity.getId());
			updateStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Update failed for " +
				entityBuilder.getEntityName() + " " +
				entity.getId() + ".",
				e
			);
		}
		
		tagDao.removeList(entity.getId());
		addTagList(entity.getId(), entity.getTagList());
	}
	
	
	/**
	 * Removes the specified entity from the database.
	 * 
	 * @param entityId
	 *            The id of the entity to remove.
	 */
	public void removeEntity(long entityId) {
		int prmIndex;
		
		tagDao.removeList(entityId);
		
		if (deleteStatement == null) {
			deleteStatement = prepareStatement(entityBuilder.getSqlDelete(true));
		}
		
		try {
			prmIndex = 1;
			deleteStatement.setLong(prmIndex++, entityId);
			deleteStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Delete failed for " +
				entityBuilder.getEntityName() + " "
				+ entityId + ".",
				e
			);
		}
	}
	
	
	/**
	 * Purges all deleted records from the database and marks all remaining
	 * records as unchanged.
	 */
	public void purgeAndResetAction() {
		if (purgeStatement == null) {
			purgeStatement = prepareStatement(entityBuilder.getSqlPurge());
		}
		
		try {
			purgeStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Purge failed for " +
				entityBuilder.getEntityName() + ".",
				e
			);
		}
		
		if (resetStatement == null) {
			resetStatement = prepareStatement(entityBuilder.getSqlResetAction());
		}
		
		try {
			resetStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Reset action failed for " +
				entityBuilder.getEntityName() + ".",
				e
			);
		}
	}
	
	
	/**
	 * Returns an iterator providing access to all entities in the database.
	 * 
	 * @return The entity iterator.
	 */
	public abstract ReleasableIterator<T> iterate();
}
