// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
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
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> getRelationMemberHistory(
			String selectedEntityStatement, SqlParameterSource parameterSource) {
		
		SimpleObjectStore<DbFeatureHistory<DbFeature<RelationMember>>> store =
			new SimpleObjectStore<DbFeatureHistory<DbFeature<RelationMember>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class), "rmb", true);
		
		try {
			String sql;
			ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<RelationMember>>> storeListener;
			DbFeatureHistoryRowMapper<DbFeature<RelationMember>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<RelationMember> dbFeatureRowMapper;
			RelationMemberRowMapper relationNodeRowMapper;
			ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> resultIterator;
			
			sql =
				"SELECT rm.id, rm.member_id, rm.member_role, rm.member_type, rm.version"
				+ " FROM "
				+ "relation_members rm"
				+ " INNER JOIN "
				+ selectedEntityStatement
				+ " t ON rm.id = t.id AND rm.version = t.version"
				+ " ORDER BY rm.id, rm.version, rm.sequence_id";
			
			LOG.log(Level.FINER, "Relation member history query: " + sql);
			
			// Sends all received data into the object store.
			storeListener = new ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<RelationMember>>>(store);
			// Retrieves the version information associated with the feature.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbFeature<RelationMember>>(storeListener);
			// Retrieves the entity information associated with the feature.
			dbFeatureRowMapper = new DbFeatureRowMapper<RelationMember>(dbFeatureHistoryRowMapper);
			// Retrieves the basic feature information.
			relationNodeRowMapper = new RelationMemberRowMapper(dbFeatureRowMapper);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			getNamedParamJdbcTemplate().query(sql, parameterSource, relationNodeRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<DbFeatureHistory<DbFeature<RelationMember>>>(store.iterate(),
					store);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			store = null;
			
			return resultIterator;
			
		} finally {
			if (store != null) {
				store.release();
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeatureHistoryPopulator<Relation, ?>> getFeatureHistoryPopulators(
			String selectedEntityTableName, MapSqlParameterSource parameterSource) {
		ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> relationNodeIterator;
		List<FeatureHistoryPopulator<Relation, ?>> featurePopulators;
		
		featurePopulators = new ArrayList<FeatureHistoryPopulator<Relation,?>>();
		
		// Get the relation nodes for the selected entities.
		relationNodeIterator = getRelationMemberHistory(selectedEntityTableName, parameterSource);
		
		// Wrap the relation node source into a feature history populator that can attach them to their
		// owning relations.
		featurePopulators.add(
				new FeatureHistoryPopulator<Relation, RelationMember>(
						relationNodeIterator, new RelationMemberCollectionLoader()));
		
		return featurePopulators;
	}
}
