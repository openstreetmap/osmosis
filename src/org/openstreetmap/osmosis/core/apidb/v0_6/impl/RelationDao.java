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
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> getRelationMemberHistory(String sql,
			SqlParameterSource parameterSource) {
		
		SimpleObjectStore<DbFeatureHistory<DbFeature<RelationMember>>> store =
			new SimpleObjectStore<DbFeatureHistory<DbFeature<RelationMember>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class), "rmb", true);
		
		try {
			ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<RelationMember>>> storeListener;
			DbFeatureHistoryRowMapper<DbFeature<RelationMember>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<RelationMember> dbFeatureRowMapper;
			RelationMemberRowMapper relationNodeRowMapper;
			ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> resultIterator;
			
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
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> getRelationMemberHistory(
			String selectedEntityTableName) {
		StringBuilder sql;
		MapSqlParameterSource parameterSource;

		sql = new StringBuilder();
		sql.append("SELECT rm.id, rm.node_id, rm.version");
		sql.append(" FROM ");
		sql.append("relation_members rm");
		sql.append("INNER JOIN ");
		sql.append(selectedEntityTableName);
		sql.append(" t ON rm.id = t.id AND rm.version = t.version");
		sql.append(" ORDER BY e.id, e.version");
		
		LOG.log(Level.FINER, "Relation member history query: " + sql);

		parameterSource = new MapSqlParameterSource();

		return getRelationMemberHistory(sql.toString(), parameterSource);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeatureHistoryPopulator<Relation, ?>> getFeatureHistoryPopulators(String selectedEntityTableName) {
		ReleasableIterator<DbFeatureHistory<DbFeature<RelationMember>>> relationNodeIterator;
		List<FeatureHistoryPopulator<Relation, ?>> featurePopulators;
		
		featurePopulators = new ArrayList<FeatureHistoryPopulator<Relation,?>>();
		
		// Get the relation nodes for the selected entities.
		relationNodeIterator = getRelationMemberHistory(selectedEntityTableName);
		
		// Wrap the relation node source into a feature history populator that can attach them to their
		// owning relations.
		featurePopulators.add(
				new FeatureHistoryPopulator<Relation, RelationMember>(
						relationNodeIterator, new RelationMemberCollectionLoader()));
		
		return featurePopulators;
	}
}
