// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsimple.common.BaseDao;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsimple.common.NoSuchRecordException;


/**
 * Provides functionality common to all top level entity daos.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityDao<T extends Entity> extends BaseDao {
	private static final Logger LOG = Logger.getLogger(EntityDao.class.getName());
	
	private EntityFeatureDao<Tag, DbFeature<Tag>> tagDao;
	private ActionDao actionDao;
	private EntityMapper<T> entityMapper;
	private PreparedStatement countStatement;
	private PreparedStatement getStatement;
	private PreparedStatement insertStatement;
	private PreparedStatement updateStatement;
	private PreparedStatement deleteStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityMapper
	 *            Provides entity type specific JDBC support.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	protected EntityDao(DatabaseContext dbCtx, EntityMapper<T> entityMapper, ActionDao actionDao) {
		super(dbCtx);
		
		this.entityMapper = entityMapper;
		this.actionDao = actionDao;
		
		tagDao = new EntityFeatureDao<Tag, DbFeature<Tag>>(dbCtx, new TagMapper(entityMapper.getEntityName()));
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
			countStatement = prepareStatement(entityMapper.getSqlSelectCount(true));
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
				"Count query failed for "
					+ entityMapper.getEntityName() + " " + entityId + ".",
				e
			);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close result set.", e);
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
			getStatement = prepareStatement(entityMapper.getSqlSelect(true, true));
		}
		
		try {
			getStatement.setLong(1, entityId);
			
			resultSet = getStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new NoSuchRecordException(entityMapper.getEntityName()
						+ " " + entityId + " doesn't exist.");
			}
			entity = entityMapper.parseRecord(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			for (DbFeature<Tag> dbTag : tagDao.getAll(entityId)) {
				entity.getTags().add(dbTag.getFeature());
			}
			
			// Add the type specific features.
			loadFeatures(entityId, entity);
			
			return entity;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Query failed for "
					+ entityMapper.getEntityName() + " " + entityId + ".",
				e
			);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close result set.", e);
				}
			}
		}
	}
	
	
	/**
	 * Adds the specified tags to the database.
	 * 
	 * @param entityId
	 *            The identifier of the entity to add these features to.
	 * @param tags
	 *            The features to add.
	 */
	private void addTags(long entityId, Collection<Tag> tags) {
		Collection<DbFeature<Tag>> dbList;
		
		dbList = new ArrayList<DbFeature<Tag>>(tags.size());
		
		for (Tag tag : tags) {
			dbList.add(new DbFeature<Tag>(entityId, tag));
		}
		
		tagDao.addAll(dbList);
	}
	
	
	/**
	 * Adds the type specific features to the entity.
	 * 
	 * @param entityId
	 *            The entity id.
	 * @param entity
	 *            The entity requiring features to be added.
	 */
	protected abstract void loadFeatures(long entityId, T entity);
	
	
	/**
	 * Adds the specified entity to the database.
	 * 
	 * @param entity
	 *            The entity to add.
	 */
	public void addEntity(T entity) {
		if (insertStatement == null) {
			insertStatement = prepareStatement(entityMapper.getSqlInsert(1));
		}
		
		try {
			entityMapper.populateEntityParameters(insertStatement, 1, entity);
			insertStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Insert failed for " + entityMapper.getEntityName()
				+ " " + entity.getId() + ".",
				e
			);
		}
		
		addTags(entity.getId(), entity.getTags());
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.CREATE, entity.getId());
	}
	
	
	/**
	 * Updates the specified entity details in the database.
	 * 
	 * @param entity
	 *            The entity to update.
	 */
	public void modifyEntity(T entity) {
		if (updateStatement == null) {
			updateStatement = prepareStatement(entityMapper.getSqlUpdate(true));
		}
		
		try {
			int prmIndex;
			
			prmIndex = 1;
			
			prmIndex = entityMapper.populateEntityParameters(updateStatement, prmIndex, entity);
			updateStatement.setLong(prmIndex++, entity.getId());
			updateStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Update failed for "
					+ entityMapper.getEntityName() + " "
					+ entity.getId() + ".",
				e
			);
		}
		
		tagDao.removeList(entity.getId());
		addTags(entity.getId(), entity.getTags());
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.MODIFY, entity.getId());
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
			deleteStatement = prepareStatement(entityMapper.getSqlDelete(true));
		}
		
		try {
			prmIndex = 1;
			deleteStatement.setLong(prmIndex++, entityId);
			deleteStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Delete failed for "
					+ entityMapper.getEntityName() + " "
					+ entityId + ".",
				e
			);
		}
		
		actionDao.addAction(entityMapper.getEntityType(), ChangesetAction.DELETE, entityId);
	}
	
	
	/**
	 * Returns an iterator providing access to all entities in the database.
	 * 
	 * @return The entity iterator.
	 */
	public abstract ReleasableIterator<T> iterate();
}
