// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainerFactory;
import org.openstreetmap.osmosis.core.database.DbFeatureHistory;
import org.openstreetmap.osmosis.core.database.DbFeatureHistoryRowMapper;
import org.openstreetmap.osmosis.core.database.DbFeatureRowMapper;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeatureHistoryComparator;
import org.openstreetmap.osmosis.core.database.DbOrderedFeatureRowMapper;
import org.openstreetmap.osmosis.core.database.RowMapperListener;
import org.openstreetmap.osmosis.core.database.SortingStoreRowMapperListener;
import org.openstreetmap.osmosis.core.database.WayNodeCollectionLoader;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;


/**
 * Provides access to ways in the database.
 */
public class WayDao extends EntityDao<Way> {
	private static final Logger LOG = Logger.getLogger(WayDao.class.getName());
	
	private static final String[] TYPE_SPECIFIC_FIELD_NAMES = new String[] {}; 


	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public WayDao(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, "way");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected RowMapperListener<CommonEntityData> getEntityRowMapper(RowMapperListener<Way> entityListener) {
		return new WayRowMapper(entityListener);
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
	protected EntityContainerFactory<Way> getContainerFactory() {
		return new WayContainerFactory();
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>> getWayNodeHistory(
			String selectedEntityStatement, SqlParameterSource parameterSource) {
		
		FileBasedSort<DbFeatureHistory<DbOrderedFeature<WayNode>>> sortingStore =
			new FileBasedSort<DbFeatureHistory<DbOrderedFeature<WayNode>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new DbOrderedFeatureHistoryComparator<WayNode>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<DbFeatureHistory<DbOrderedFeature<WayNode>>> storeListener;
			DbFeatureHistoryRowMapper<DbOrderedFeature<WayNode>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<WayNode> dbFeatureRowMapper;
			DbOrderedFeatureRowMapper<WayNode> dbOrderedFeatureRowMapper;
			WayNodeRowMapper wayNodeRowMapper;
			ReleasableIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>> resultIterator;
			
			sql =
				"SELECT wn.way_id AS id, wn.node_id, wn.version, wn.sequence_id"
				+ " FROM "
				+ "way_nodes wn"
				+ " INNER JOIN "
				+ selectedEntityStatement
				+ " t ON wn.way_id = t.way_id AND wn.version = t.version";
			
			LOG.log(Level.FINER, "Way node history query: " + sql);
			
			// Sends all received data into the object store.
			storeListener =
				new SortingStoreRowMapperListener<DbFeatureHistory<DbOrderedFeature<WayNode>>>(sortingStore);
			// Retrieves the version information associated with the feature.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbOrderedFeature<WayNode>>(storeListener);
			// Retrieves the sequence number associated with the feature.
			dbOrderedFeatureRowMapper = new DbOrderedFeatureRowMapper<WayNode>(dbFeatureHistoryRowMapper);
			// Retrieves the entity information associated with the feature.
			dbFeatureRowMapper = new DbFeatureRowMapper<WayNode>(dbOrderedFeatureRowMapper);
			// Retrieves the basic feature information.
			wayNodeRowMapper = new WayNodeRowMapper(dbFeatureRowMapper);
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			getNamedParamJdbcTemplate().query(sql, parameterSource, wayNodeRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator =
				new StoreReleasingIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>>(
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
	protected List<FeatureHistoryPopulator<Way, ?, ?>> getFeatureHistoryPopulators(
			String selectedEntityStatement, MapSqlParameterSource parameterSource) {
		
		ReleasableIterator<DbFeatureHistory<DbOrderedFeature<WayNode>>> wayNodeIterator;
		List<FeatureHistoryPopulator<Way, ?, ?>> featurePopulators;
		
		featurePopulators = new ArrayList<FeatureHistoryPopulator<Way, ?, ?>>();
		
		// Get the way nodes for the selected entities.
		wayNodeIterator = getWayNodeHistory(selectedEntityStatement, parameterSource);
		
		// Wrap the way node source into a feature history populator that can attach them to their
		// owning ways.
		featurePopulators.add(
				new FeatureHistoryPopulator<Way, WayNode, DbOrderedFeature<WayNode>>(
						wayNodeIterator, new WayNodeCollectionLoader()));
		
		return featurePopulators;
	}
}
