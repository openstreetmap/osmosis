// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * Provides functionality common to all top level entity daos.
 * 
 * @param <T>
 *            The entity type to be supported.
 */
public abstract class EntityDao<T extends Entity> {
	private static final Logger LOG = Logger.getLogger(EntityDao.class.getName());

	private JdbcTemplate jdbcTemplate;
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
		this.jdbcTemplate = jdbcTemplate;
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
	 * @param selectedEntityStatement
	 *            The statement for obtaining the id and version pairs of entity records selected.
	 * @param parameterSource
	 *            The parameters required to execute the selected entity statement.
	 * @return The history feature populators.
	 */
	protected abstract List<FeatureHistoryPopulator<T, ?, ?>> getFeatureHistoryPopulators(
			String selectedEntityStatement, MapSqlParameterSource parameterSource);
	
	
	private String buildFeaturelessEntityHistoryQuery(String selectedEntityStatement) {
		StringBuilder sql;

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
		sql.append(selectedEntityStatement);
		sql.append(" t ON e.id = t.id AND e.version = t.version");
		sql.append(" INNER JOIN changesets c ON e.changeset_id = c.id INNER JOIN users u ON c.user_id = u.id");
		
		LOG.log(Level.FINER, "Entity history query: " + sql);

		return sql.toString();
	}


	private ReleasableIterator<EntityHistory<T>> getFeaturelessEntityHistory(
			String selectedEntityStatement, MapSqlParameterSource parameterSource) {
		
		FileBasedSort<EntityHistory<T>> sortingStore =
			new FileBasedSort<EntityHistory<T>>(
				new SingleClassObjectSerializationFactory(EntityHistory.class),
				new EntityHistoryComparator<T>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<EntityHistory<T>> storeListener;
			EntityHistoryRowMapper<T> entityHistoryRowMapper;
			RowMapperListener<CommonEntityData> entityRowMapper;
			EntityDataRowMapper entityDataRowMapper;
			ReleasableIterator<EntityHistory<T>> resultIterator;
			
			sql = buildFeaturelessEntityHistoryQuery(selectedEntityStatement);
			
			// Sends all received data into the object store.
			storeListener = new SortingStoreRowMapperListener<EntityHistory<T>>(sortingStore);
			// Retrieves the visible attribute allowing modifies to be distinguished
			// from deletes.
			entityHistoryRowMapper = new EntityHistoryRowMapper<T>(storeListener);
			// Retrieves the entity type specific columns and builds the entity objects.
			entityRowMapper = getEntityRowMapper(entityHistoryRowMapper);
			// Retrieves the common entity information.
			entityDataRowMapper = new EntityDataRowMapper(entityRowMapper, false);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			namedParamJdbcTemplate.query(sql, parameterSource, entityDataRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<EntityHistory<T>>(sortingStore.iterate(), sortingStore);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			sortingStore = null;
			
			return resultIterator;
			
		} finally {
			if (sortingStore != null) {
				sortingStore.release();
			}
		}
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> getTagHistory(String selectedEntityStatement,
			MapSqlParameterSource parameterSource) {
		
		FileBasedSort<DbFeatureHistory<DbFeature<Tag>>> sortingStore =
			new FileBasedSort<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new DbFeatureHistoryComparator<Tag>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<DbFeatureHistory<DbFeature<Tag>>> storeListener;
			DbFeatureHistoryRowMapper<DbFeature<Tag>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<Tag> dbFeatureRowMapper;
			TagRowMapper tagRowMapper;
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> resultIterator;
			
			sql =
				"SELECT et.id, et.k, et.v, et.version"
				+ " FROM "
				+ entityName
				+ "_tags et"
				+ " INNER JOIN "
				+ selectedEntityStatement
				+ " t ON et.id = t.id AND et.version = t.version";
			
			LOG.log(Level.FINER, "Tag history query: " + sql);
			
			// Sends all received data into the object store.
			storeListener = new SortingStoreRowMapperListener<DbFeatureHistory<DbFeature<Tag>>>(sortingStore);
			// Retrieves the version information associated with the tag.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbFeature<Tag>>(storeListener);
			// Retrieves the entity information associated with the tag.
			dbFeatureRowMapper = new DbFeatureRowMapper<Tag>(dbFeatureHistoryRowMapper);
			// Retrieves the basic tag information.
			tagRowMapper = new TagRowMapper(dbFeatureRowMapper);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			namedParamJdbcTemplate.query(sql, parameterSource, tagRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<DbFeatureHistory<DbFeature<Tag>>>(sortingStore.iterate(),
					sortingStore);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			sortingStore = null;
			
			return resultIterator;
			
		} finally {
			if (sortingStore != null) {
				sortingStore.release();
			}
		}
	}
	
	
	private ReleasableIterator<EntityHistory<T>> getEntityHistory(
			String selectedEntityStatement, MapSqlParameterSource parameterSource) {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			ReleasableIterator<EntityHistory<T>> entityIterator;
			ReleasableIterator<DbFeatureHistory<DbFeature<Tag>>> tagIterator;
			List<FeatureHistoryPopulator<T, ?, ?>> featurePopulators;
			EntityHistoryReader<T> entityHistoryReader;
			
			// PostgreSQL sometimes incorrectly chooses to perform full table scans, this option
			// prevents this. Note that this is not recommended practice according to documentation
			// but fixing this would either require modifying the table statistics gathering
			// configuration on the production database or figuring out a way of disabling server
			// side prepared statements.
			jdbcTemplate.execute("set local enable_seqscan = false");
			jdbcTemplate.execute("set local enable_mergejoin = false");
			jdbcTemplate.execute("set local enable_hashjoin = false");
			
			entityIterator = releasableContainer.add(
					getFeaturelessEntityHistory(selectedEntityStatement, parameterSource));
			tagIterator = releasableContainer.add(
					getTagHistory(selectedEntityStatement, parameterSource));
			
			featurePopulators = getFeatureHistoryPopulators(selectedEntityStatement, parameterSource);
			for (FeatureHistoryPopulator<T, ?, ?> featurePopulator : featurePopulators) {
				releasableContainer.add(featurePopulator);
			}
			
			entityHistoryReader = new EntityHistoryReader<T>(entityIterator, tagIterator, featurePopulators);
			
			// The sources are now all attached to the history reader so we don't want to release
			// them in the finally block.
			releasableContainer.clear();
			
			return entityHistoryReader;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	private ReleasableIterator<ChangeContainer> getChangeHistory(
			String selectedEntityStatement, MapSqlParameterSource parameterSource) {
		
		return new ChangeReader<T>(getEntityHistory(selectedEntityStatement, parameterSource), getContainerFactory());
	}
	
	
	private List<Integer> buildTransactionRanges(long bottomTransactionId, long topTransactionId) {
		List<Integer> rangeValues;
		int topTransactionIdInt;
		int currentXid;
		
		// We need the top transaction id in int form so that we can tell if it will be treated as a
		// negative number.
		topTransactionIdInt = (int) topTransactionId;
		
		// Begin building the values to use in the WHERE clause transaction ranges. Each pair of ids
		// in this list will become a range selection.
		rangeValues = new ArrayList<Integer>();
		
		// The bottom id is the last one read, so we begin reading from the next transaction.
		currentXid = ((int) bottomTransactionId) + 1;
		rangeValues.add(currentXid);
		
		// We only have data to process if the two transaction ids are not equal.
		if (currentXid != topTransactionId) {
			
			// Process until we have enough ranges to reach the top transaction id.
			while (currentXid != topTransactionIdInt) {
				// Determine how to terminate the current transaction range.
				if (currentXid <= 2 && topTransactionId >= 0) {
					// The range overlaps special ids 0-2 which should never be queried on.
					
					// Terminate the current range before the special values.
					rangeValues.add(-1);
					// Begin the new range after the special values.
					rangeValues.add(3);
					
					currentXid = 3;
					
				} else if (topTransactionIdInt < currentXid) {
					// The range crosses the integer overflow point. Only do this check once we are
					// past 2 because the xid special values 0-2 may need to be crossed first.
					
					// Terminate the current range at the maximum int value.
					rangeValues.add(Integer.MAX_VALUE);
					// Begin a new range at the minimum int value.
					rangeValues.add(Integer.MIN_VALUE);
					
					currentXid = Integer.MIN_VALUE;
					
				} else {
					// There are no problematic transaction id values between the current value and
					// the top transaction id so terminate the current range at the top transaction
					// id.
					rangeValues.add(topTransactionIdInt);
					currentXid = topTransactionIdInt;
				}
			}
			
		} else {
			// Terminate the range at the top transaction id. The start of the range is one higher
			// therefore no data will be selected.
			rangeValues.add(topTransactionIdInt);
		}
		
		return rangeValues;
	}
	
	
	private void buildTransactionRangeWhereClause(StringBuilder sql, MapSqlParameterSource parameters,
			long bottomTransactionId, long topTransactionId) {
		List<Integer> rangeValues;
		
		// Determine the transaction ranges required to select all transactions between the bottom
		// and top values. This takes into account transaction id wrapping issues and reserved ids.
		rangeValues = buildTransactionRanges(bottomTransactionId, topTransactionId);
		
		// Create a range clause for each range pair.
		for (int i = 0; i < rangeValues.size(); i = i + 2) {
			if (i > 0) {
				sql.append(" OR ");
			}
			sql.append("(");
			sql.append("xid_to_int4(xmin) >= :rangeValue").append(i);
			sql.append(" AND ");
			sql.append("xid_to_int4(xmin) <= :rangeValue").append(i + 1);
			sql.append(")");
			
			parameters.addValue("rangeValue" + i, rangeValues.get(i), Types.INTEGER);
			parameters.addValue("rangeValue" + (i + 1), rangeValues.get(i + 1), Types.INTEGER);
		}
	}
	
	
	private void buildTransactionIdListWhereClause(StringBuilder sql, List<Long> transactionIdList) {
		for (int i = 0; i < transactionIdList.size(); i++) {
			if (i > 0) {
				sql.append(",");
			}
		
			// Must cast to int to allow the integer based xmin index to be used correctly.
			sql.append((int) transactionIdList.get(i).longValue());
		}
	}


	/**
	 * Retrieves the changes that have were made by a set of transactions.
	 * 
	 * @param predicates
	 *            Contains the predicates defining the transactions to be queried.
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory(ReplicationQueryPredicates predicates) {
		String selectedEntityStatement;
		StringBuilder sql;
		MapSqlParameterSource parameterSource;
		
		parameterSource = new MapSqlParameterSource();
		
		selectedEntityStatement = "tmp_" + entityName + "s";
		
		sql = new StringBuilder();
		sql.append("CREATE TEMPORARY TABLE ");
		sql.append(selectedEntityStatement);
		sql.append(" ON COMMIT DROP");
		sql.append(" AS SELECT id, version FROM ");
		sql.append(entityName);
		sql.append("s WHERE ((");
		// Add the main transaction ranges to the where clause.
		buildTransactionRangeWhereClause(sql, parameterSource, predicates.getBottomTransactionId(), predicates
				.getTopTransactionId());
		sql.append(")");
		// If previously active transactions have become ready since the last invocation we include those as well.
		if (predicates.getReadyList().size() > 0) {
			sql.append(" OR xid_to_int4(xmin) IN [");
			buildTransactionIdListWhereClause(sql, predicates.getReadyList());
			sql.append("]");
		}
		sql.append(")");
		// Any active transactions must be explicitly excluded.
		if (predicates.getActiveList().size() > 0) {
			sql.append(" AND xid_to_int4(xmin) NOT IN [");
			buildTransactionIdListWhereClause(sql, predicates.getActiveList());
			sql.append("]");
		}
		
		LOG.log(Level.FINER, "Entity identification query: " + sql);
		
		namedParamJdbcTemplate.update(sql.toString(), parameterSource);
		
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER,
					jdbcTemplate.queryForInt("SELECT Count(id) FROM " + selectedEntityStatement) + " "
					+ entityName + " records located.");
		}
		
		return getChangeHistory(selectedEntityStatement, new MapSqlParameterSource());
	}


	/**
	 * Retrieves the changes that have were made between two points in time.
	 * 
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory(Date intervalBegin, Date intervalEnd) {
		String sql;
		MapSqlParameterSource parameterSource;
		
		sql =
			"(SELECT id, version FROM "
			+ entityName
			+ "s WHERE timestamp > :intervalBegin AND timestamp <= :intervalEnd)";
		
		LOG.log(Level.FINER, "Entity identification query: " + sql);

		parameterSource = new MapSqlParameterSource();
		parameterSource.addValue("intervalBegin", intervalBegin, Types.TIMESTAMP);
		parameterSource.addValue("intervalEnd", intervalEnd, Types.TIMESTAMP);
		
		return getChangeHistory(sql, parameterSource);
	}
	
	
	/**
	 * Retrieves all changes in the database.
	 * 
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory() {
		// Join the entity table to itself which will return all records.
		return getChangeHistory(entityName + "s", new MapSqlParameterSource());
	}
	
	
	/**
	 * Retrieves all current data in the database.
	 * 
	 * @return An iterator pointing at the current records.
	 */
	public ReleasableIterator<EntityContainer> getCurrent() {
		// Join the entity table to the current version of itself which will return all current
		// records.
		return new EntityContainerReader<T>(
				getEntityHistory("(SELECT id, version FROM current_" + entityName + "s WHERE visible = TRUE)",
						new MapSqlParameterSource()), getContainerFactory());
	}
}
