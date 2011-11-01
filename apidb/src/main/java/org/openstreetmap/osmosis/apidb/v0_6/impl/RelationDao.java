// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainerFactory;
import org.openstreetmap.osmosis.core.database.DbFeatureHistory;
import org.openstreetmap.osmosis.core.database.DbFeatureHistoryRowMapper;
import org.openstreetmap.osmosis.core.database.DbFeatureRowMapper;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeatureHistoryComparator;
import org.openstreetmap.osmosis.core.database.DbOrderedFeatureRowMapper;
import org.openstreetmap.osmosis.core.database.RelationMemberCollectionLoader;
import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.database.SortingStoreRowMapperListener;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;


/**
 * Provides access to relations in the database.
 */
public class RelationDao extends EntityDao<Relation> {
	private static final Logger LOG = Logger.getLogger(RelationDao.class.getName());
	
	private static final String[] TYPE_SPECIFIC_FIELD_NAMES = new String[] {}; 


	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public RelationDao(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, "relation");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RowMapperListener<CommonEntityData> getEntityRowMapper(RowMapperListener<Relation> entityListener) {
		return new RelationRowMapper(entityListener);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] getTypeSpecificFieldNames() {
		return TYPE_SPECIFIC_FIELD_NAMES;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityContainerFactory<Relation> getContainerFactory() {
		return new RelationContainerFactory();
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>> getRelationMemberHistory(
			String selectedEntityStatement, SqlParameterSource parameterSource) {
		
		FileBasedSort<DbFeatureHistory<DbOrderedFeature<RelationMember>>> sortingStore =
			new FileBasedSort<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new DbOrderedFeatureHistoryComparator<RelationMember>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<DbFeatureHistory<DbOrderedFeature<RelationMember>>> storeListener;
			DbFeatureHistoryRowMapper<DbOrderedFeature<RelationMember>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<RelationMember> dbFeatureRowMapper;
			DbOrderedFeatureRowMapper<RelationMember> dbOrderedFeatureRowMapper;
			RelationMemberRowMapper relationNodeRowMapper;
			ReleasableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>> resultIterator;
			
			sql =
				"SELECT rm.relation_id AS id, rm.member_id, rm.member_role, rm.member_type, rm.version, rm.sequence_id"
				+ " FROM "
				+ "relation_members rm"
				+ " INNER JOIN "
				+ selectedEntityStatement
				+ " t ON rm.relation_id = t.relation_id AND rm.version = t.version";
			
			LOG.log(Level.FINER, "Relation member history query: " + sql);
			
			// Sends all received data into the object store.
			storeListener =
				new SortingStoreRowMapperListener<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(sortingStore);
			// Retrieves the version information associated with the feature.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbOrderedFeature<RelationMember>>(storeListener);
			// Retrieves the sequence number associated with the feature.
			dbOrderedFeatureRowMapper = new DbOrderedFeatureRowMapper<RelationMember>(dbFeatureHistoryRowMapper);
			// Retrieves the entity information associated with the feature.
			dbFeatureRowMapper = new DbFeatureRowMapper<RelationMember>(dbOrderedFeatureRowMapper);
			// Retrieves the basic feature information.
			relationNodeRowMapper = new RelationMemberRowMapper(dbFeatureRowMapper);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			getNamedParamJdbcTemplate().query(sql, parameterSource, relationNodeRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(
					sortingStore.iterate(),
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeatureHistoryPopulator<Relation, ?, ?>> getFeatureHistoryPopulators(
			String selectedEntityTableName, MapSqlParameterSource parameterSource) {
		ReleasableIterator<DbFeatureHistory<DbOrderedFeature<RelationMember>>> relationNodeIterator;
		List<FeatureHistoryPopulator<Relation, ?, ?>> featurePopulators;
		
		featurePopulators = new ArrayList<FeatureHistoryPopulator<Relation, ?, ?>>();
		
		// Get the relation nodes for the selected entities.
		relationNodeIterator = getRelationMemberHistory(selectedEntityTableName, parameterSource);
		
		// Wrap the relation node source into a feature history populator that can attach them to their
		// owning relations.
		featurePopulators.add(
				new FeatureHistoryPopulator<Relation, RelationMember, DbOrderedFeature<RelationMember>>(
						relationNodeIterator, new RelationMemberCollectionLoader()));
		
		return featurePopulators;
	}
}
