// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainerFactory;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainerFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
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
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<WayNode>>> getWayNodeHistory(String sql,
			SqlParameterSource parameterSource) {
		
		SimpleObjectStore<DbFeatureHistory<DbFeature<WayNode>>> store =
			new SimpleObjectStore<DbFeatureHistory<DbFeature<WayNode>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class), "wnd", true);
		
		try {
			ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<WayNode>>> storeListener;
			DbFeatureHistoryRowMapper<DbFeature<WayNode>> dbFeatureHistoryRowMapper;
			DbFeatureRowMapper<WayNode> dbFeatureRowMapper;
			WayNodeRowMapper wayNodeRowMapper;
			ReleasableIterator<DbFeatureHistory<DbFeature<WayNode>>> resultIterator;
			
			// Sends all received data into the object store.
			storeListener = new ObjectStoreRowMapperListener<DbFeatureHistory<DbFeature<WayNode>>>(store);
			// Retrieves the version information associated with the feature.
			dbFeatureHistoryRowMapper = new DbFeatureHistoryRowMapper<DbFeature<WayNode>>(storeListener);
			// Retrieves the entity information associated with the feature.
			dbFeatureRowMapper = new DbFeatureRowMapper<WayNode>(dbFeatureHistoryRowMapper);
			// Retrieves the basic feature information.
			wayNodeRowMapper = new WayNodeRowMapper(dbFeatureRowMapper);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			getNamedParamJdbcTemplate().query(sql, parameterSource, wayNodeRowMapper);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator = new StoreReleasingIterator<DbFeatureHistory<DbFeature<WayNode>>>(store.iterate(), store);
			
			// The store itself shouldn't be released now that it has been attached to the iterator.
			store = null;
			
			return resultIterator;
			
		} finally {
			if (store != null) {
				store.release();
			}
		}
	}
	
	
	private ReleasableIterator<DbFeatureHistory<DbFeature<WayNode>>> getWayNodeHistory(String selectedEntityTableName) {
		StringBuilder sql;
		MapSqlParameterSource parameterSource;

		sql = new StringBuilder();
		sql.append("SELECT wn.id, wn.node_id, wn.version");
		sql.append(" FROM ");
		sql.append("way_nodes wn");
		sql.append("INNER JOIN ");
		sql.append(selectedEntityTableName);
		sql.append(" t ON wn.id = t.id AND wn.version = t.version");
		sql.append(" ORDER BY e.id, e.version");
		
		LOG.log(Level.FINER, "Way node history query: " + sql);

		parameterSource = new MapSqlParameterSource();

		return getWayNodeHistory(sql.toString(), parameterSource);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<FeatureHistoryPopulator<Way, ?>> getFeatureHistoryPopulators(String selectedEntityTableName) {
		ReleasableIterator<DbFeatureHistory<DbFeature<WayNode>>> wayNodeIterator;
		List<FeatureHistoryPopulator<Way, ?>> featurePopulators;
		
		featurePopulators = new ArrayList<FeatureHistoryPopulator<Way,?>>();
		
		// Get the way nodes for the selected entities.
		wayNodeIterator = getWayNodeHistory(selectedEntityTableName);
		
		// Wrap the way node source into a feature history populator that can attach them to their
		// owning ways.
		featurePopulators.add(
				new FeatureHistoryPopulator<Way, WayNode>(
						wayNodeIterator, new WayNodeCollectionLoader()));
		
		return featurePopulators;
	}
}
