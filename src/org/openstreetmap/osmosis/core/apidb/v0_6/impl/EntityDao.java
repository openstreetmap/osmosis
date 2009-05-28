// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;


/**
 * Provides functionality common to all top level entity daos.
 * 
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityDao<T extends Entity> {
	private static final Logger LOG = Logger.getLogger(EntityDao.class.getName());

	private NamedParameterJdbcTemplate namedParamJdbcTemplate;
	private String entityName;


	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 * @param entityName
	 *            The name of the entity. Used for building dynamic sql queries.
	 */
	protected EntityDao(JdbcTemplate jdbcTemplate, String entityName) {
		this.namedParamJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		this.entityName = entityName;
	}
	
	
	/**
	 * Provides access to the named parameter jdbc template.
	 * 
	 * @return The jdbc template.
	 */
	protected NamedParameterJdbcTemplate getNamedParamJdbcTemplate() {
		return namedParamJdbcTemplate;
	}


	/**
	 * Produces an array of additional column names specific to this entity type to be returned by
	 * entity queries.
	 * 
	 * @return The column names.
	 */
	protected abstract String[] getTypeSpecificFieldNames();
	
	
	/**
	 * Creates a row mapper that receives common entity data objects and produces full entity
	 * objects.
	 * 
	 * @param entityListener
	 *            The full entity object listener.
	 * @return The full entity row mapper.
	 */
	protected abstract RowMapperListener<CommonEntityData> getEntityRowMapper(RowMapperListener<T> entityListener);
	
	
	/**
	 * Gets the entity container factory for the entity type.
	 * 
	 * @return The factory.
	 */
	protected abstract EntityContainerFactory<T> getContainerFactory();


	/**
	 * Gets the history feature populators for the entity type.
	 * 
	 * @param selectedEntityTableName
	 *            The name of the table containing the id and version pairs of entity records
	 *            selected.
	 * @return The history feature populators.
	 */
	protected abstract List<FeatureHistoryPopulator<T, ?>> getFeatureHistoryPopulators(String selectedEntityTableName);
	
	
	private ReleasableIterator<EntityHistory<T>> getEntityHistory(String sql, SqlParameterSource parameterSource) {
		SimpleObjectStore<EntityHistory<T>> store = new SimpleObjectStore<EntityHistory<T>>(
				new SingleClassObjectSerializationFactory(EntityHistory.class), "his", true);
		
		try {
			ObjectStoreRowMapperListener<EntityHistory<T>> storeListener;
			EntityHistoryRowMapper<T> entityHistoryRowMapper;
			RowMapperListener<CommonEntityData> entityRowMapper;
			EntityDataRowMapper entityDataRowMapper;
			ReleasableIterator<EntityHistory<T>> resultIterator;
			
			// Sends all received data into the object store.
			storeListener = new ObjectStoreRowMapperListener<EntityHistory<T>>(store);
			// Retrieves the visible attribute allowing modifies to be distinguished
			// from deletes.
			entityHistoryRowMapper = new EntityHistoryRowMapper<T>(storeListener);
			// Retrieves the entity type specific columns and builds the entity objects.
			entityRowMapper = getEntityRowMapper(entityHistoryRowMapper);
			// Retrieves the common entity information.
			entityDataRowMapper = new EntityDataRowMapper(entityRowMapper, true);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			namedParamJdbcTemplate.query(sql, parameterSource, entityDataRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<EntityHistory<T>>(store.iterate(), store);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			store = null;
			
			return resultIterator;
			
		} finally {
			if (store != null) {
				store.release();
			}
		}
	}


	private ReleasableIterator<EntityHistory<T>> getEntityHistory(String selectedEntityTableName) {
		StringBuilder sql;
		MapSqlParameterSource parameterSource;

		sql = new StringBuilder();
		sql.append("SELECT e.id, e.version, e.timestamp, e.visible, u.data_public,");
		sql.append(" u.id AS user_id, u.display_name, e.changeset_id");

		for (String fieldName : getTypeSpecificFieldNames()) {
			sql.append(", e.");
			sql.append(fieldName);
		}

		sql.append(" FROM ");
		sql.append(entityName);
		sql.append("s e");
		sql.append(" INNER JOIN ");
		sql.append(selectedEntityTableName);
		sql.append(" t ON e.id = t.id AND e.version = t.version");
		sql.append(" INNER JOIN changesets c ON e.changeset_id = c.id INNER JOIN users u ON c.user_id = u.id");
		sql.append(" ORDER BY e.id, e.version");
		
		LOG.log(Level.FINER, "Entity history query: " + sql);

		parameterSource = new MapSqlParameterSource();

		return getEntityHistory(sql.toString(), parameterSource);
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> getTagHistory(String sql,
			SqlParameterSource parameterSource) {
		
		SimpleObjectStore<DbFeatureHistory<DbFeature<Tag>>> store =
			new SimpleObjectStore<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class), "tag", true);
		
		try {
			ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<Tag>>> storeListener;
			DbFeatureHistoryRowMapper<DbFeature<Tag>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<Tag> dbFeatureRowMapper;
			TagRowMapper tagRowMapper;
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> resultIterator;
			
			// Sends all received data into the object store.
			storeListener = new ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<Tag>>>(store);
			// Retrieves the version information associated with the tag.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbFeature<Tag>>(storeListener);
			// Retrieves the entity information associated with the tag.
			dbFeatureRowMapper = new DbFeatureRowMapper<Tag>(dbFeatureHistoryRowMapper);
			// Retrieves the basic tag information.
			tagRowMapper = new TagRowMapper(dbFeatureRowMapper);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			namedParamJdbcTemplate.query(sql, parameterSource, tagRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<DbFeatureHistory<DbFeature<Tag>>>(store.iterate(), store);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			store = null;
			
			return resultIterator;
			
		} finally {
			if (store != null) {
				store.release();
			}
		}
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> getTagHistory(String selectedEntityTableName) {
		StringBuilder sql;
		MapSqlParameterSource parameterSource;

		sql = new StringBuilder();
		sql.append("SELECT et.id, et.k, et.v, et.version");
		sql.append(" FROM ");
		sql.append(entityName);
		sql.append("_tags et");
		sql.append(" INNER JOIN ");
		sql.append(selectedEntityTableName);
		sql.append(" t ON et.id = t.id AND et.version = t.version");
		sql.append(" ORDER BY et.id, et.version");
		
		LOG.log(Level.FINER, "Tag history query: " + sql);

		parameterSource = new MapSqlParameterSource();

		return getTagHistory(sql.toString(), parameterSource);
	}
	
	
	private ReleasableIterator<ChangeContainer> getHistory(String selectedEntityTableName) {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			ReleasableIterator<EntityHistory<T>> entityIterator;
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> tagIterator;
			List<FeatureHistoryPopulator<T, ?>> featurePopulators;
			EntityHistoryReader2<T> entityHistoryReader;
			EntityChangeReader<T> entityChangeReader;
			
			entityIterator = releasableContainer.add(getEntityHistory(selectedEntityTableName));
			tagIterator = releasableContainer.add(getTagHistory(selectedEntityTableName));
			
			featurePopulators = getFeatureHistoryPopulators(selectedEntityTableName);
			for (FeatureHistoryPopulator<T, ?> featurePopulator : featurePopulators) {
				releasableContainer.add(featurePopulator);
			}
			
			entityHistoryReader = new EntityHistoryReader2<T>(entityIterator, tagIterator, featurePopulators);
			entityChangeReader = new EntityChangeReader<T>(entityHistoryReader, getContainerFactory());
			
			// The sources are now all attached to the history reader so we don't want to release
			// them in the finally block.
			releasableContainer.clear();
			
			return entityChangeReader;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * Retrieves the changes that have were made by a set of transactions.
	 * 
	 * @param baseTimestamp
	 *            The timestamp to constrain the query by. This timestamp is included for
	 *            performance reasons and limits the amount of data searched for the transaction
	 *            ids.
	 * @param txnList
	 *            The set of transactions to query for.
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory(Date baseTimestamp, List<Long> txnList) {
		String selectedEntityTableName;
		StringBuilder sql;
		MapSqlParameterSource parameterSource;
		
		selectedEntityTableName = "tmp_" + entityName + "s";
		
		sql = new StringBuilder();
		sql.append("CREATE TEMPORARY TABLE ");
		sql.append(selectedEntityTableName);
		sql.append(" AS SELECT id, version FROM ");
		sql.append(entityName);
		sql.append("s WHERE timestamp > :baseTimestamp AND xmin IN [:txnList]");
		sql.append(" ON COMMIT DROP");
		
		LOG.log(Level.FINER, "Entity identification query: " + sql);

		parameterSource = new MapSqlParameterSource();
		parameterSource.addValue("baseTimestamp", baseTimestamp, Types.TIMESTAMP);
		parameterSource.addValue("txnList", txnList, Types.BIGINT);
		
		namedParamJdbcTemplate.update(sql.toString(), parameterSource);
		
		return getHistory(selectedEntityTableName);
	}
	
	
	/**
	 * Retrieves all changes in the database.
	 * 
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory() {
		// Join the entity table to itself which will return all records.
		return getHistory(entityName + "s");
	}
}
