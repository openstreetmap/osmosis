// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.database.FeaturePopulator;
import org.openstreetmap.osmosis.core.database.SortingStoreRowMapperListener;
import org.openstreetmap.osmosis.core.database.WayNodeCollectionLoader;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.sort.common.FileBasedSort;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.StoreReleasingIterator;
import org.openstreetmap.osmosis.core.store.UpcastIterator;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext2;
import org.openstreetmap.osmosis.pgsnapshot.common.RowMapperRowCallbackListener;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao extends EntityDao<Way> {
	
	private static final String SQL_UPDATE_WAY_BBOX =
		"UPDATE ways SET bbox = ("
		+ " SELECT Envelope(Collect(geom))"
		+ " FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id"
		+ " WHERE way_nodes.way_id = ways.id"
		+ " )"
		+ " WHERE ways.id = ?";
	private static final String SQL_UPDATE_WAY_LINESTRING =
		"UPDATE ways w SET linestring = ("
		+ " SELECT MakeLine(c.geom) AS way_line FROM ("
		+ " SELECT n.geom AS geom FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id"
		+ " WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id"
		+ " ) c"
		+ " )"
		+ " WHERE w.id  = ?";
	
	private SimpleJdbcTemplate jdbcTemplate;
	private DatabaseCapabilityChecker capabilityChecker;
	private EntityFeatureDao<WayNode, DbOrderedFeature<WayNode>> wayNodeDao;
	private WayNodeMapper wayNodeMapper;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public WayDao(DatabaseContext2 dbCtx, ActionDao actionDao) {
		super(dbCtx.getSimpleJdbcTemplate(), new WayMapper(), actionDao);
		
		jdbcTemplate = dbCtx.getSimpleJdbcTemplate();
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
		wayNodeMapper = new WayNodeMapper();
		wayNodeDao = new EntityFeatureDao<WayNode, DbOrderedFeature<WayNode>>(jdbcTemplate, wayNodeMapper);
	}
	
	
	private void loadFeatures(long entityId, Way entity) {
		entity.getWayNodes().addAll(wayNodeDao.getAllRaw(entityId));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getEntity(long entityId) {
		Way entity;
		
		entity = super.getEntity(entityId);
		
		loadFeatures(entityId, entity);
		
		return entity;
	}


	/**
	 * Adds the specified way node list to the database.
	 * 
	 * @param entityId
	 *            The identifier of the entity to add these features to.
	 * @param wayNodeList
	 *            The list of features to add.
	 */
	private void addWayNodeList(long entityId, List<WayNode> wayNodeList) {
		List<DbOrderedFeature<WayNode>> dbList;
		
		dbList = new ArrayList<DbOrderedFeature<WayNode>>(wayNodeList.size());
		
		for (int i = 0; i < wayNodeList.size(); i++) {
			dbList.add(new DbOrderedFeature<WayNode>(entityId, wayNodeList.get(i), i));
		}
		
		wayNodeDao.addAll(dbList);
	}
	
	
	/**
	 * Updates the bounding box column for the specified way.
	 * 
	 * @param wayId
	 *            The way bounding box.
	 */
	private void updateWayGeometries(long wayId) {
		if (capabilityChecker.isWayBboxSupported()) {
			jdbcTemplate.update(SQL_UPDATE_WAY_BBOX, wayId);
		}
		if (capabilityChecker.isWayLinestringSupported()) {
			jdbcTemplate.update(SQL_UPDATE_WAY_LINESTRING, wayId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Way entity) {
		super.addEntity(entity);
		
		addWayNodeList(entity.getId(), entity.getWayNodes());
		
		updateWayGeometries(entity.getId());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Way entity) {
		long wayId;
		
		super.modifyEntity(entity);
		
		wayId = entity.getId();
		wayNodeDao.removeList(wayId);
		addWayNodeList(entity.getId(), entity.getWayNodes());
		
		updateWayGeometries(entity.getId());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntity(long entityId) {
		wayNodeDao.removeList(entityId);
		
		super.removeEntity(entityId);
	}
	
	
	private ReleasableIterator<DbOrderedFeature<WayNode>> getWayNodes(String tablePrefix) {
		
		FileBasedSort<DbOrderedFeature<WayNode>> sortingStore =
			new FileBasedSort<DbOrderedFeature<WayNode>>(
				new SingleClassObjectSerializationFactory(DbOrderedFeature.class),
				new DbOrderedFeatureComparator<WayNode>(), true);
		
		try {
			String sql;
			SortingStoreRowMapperListener<DbOrderedFeature<WayNode>> storeListener;
			RowMapperRowCallbackListener<DbOrderedFeature<WayNode>> rowCallbackListener;
			ReleasableIterator<DbOrderedFeature<WayNode>> resultIterator;
			
			sql = wayNodeMapper.getSqlSelect(tablePrefix, false, false);
			
			// Sends all received data into the object store.
			storeListener = new SortingStoreRowMapperListener<DbOrderedFeature<WayNode>>(sortingStore);
			// Converts result set rows into objects and passes them into the store.
			rowCallbackListener = new RowMapperRowCallbackListener<DbOrderedFeature<WayNode>>(wayNodeMapper
					.getRowMapper(), storeListener);
			
			// Perform the query passing the row mapper chain to process rows in a streamy fashion.
			jdbcTemplate.getJdbcOperations().query(sql, rowCallbackListener);
			
			// Open a iterator on the store that will release the store upon completion.
			resultIterator =
				new StoreReleasingIterator<DbOrderedFeature<WayNode>>(sortingStore.iterate(), sortingStore);
			
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
	protected List<FeaturePopulator<Way>> getFeaturePopulators(String tablePrefix) {
		ReleasableIterator<DbFeature<WayNode>> wayNodeIterator;
		List<FeaturePopulator<Way>> featurePopulators;
		
		featurePopulators = new ArrayList<FeaturePopulator<Way>>();
		
		// Get the way nodes for the selected entities.
		wayNodeIterator = new UpcastIterator<DbFeature<WayNode>, DbOrderedFeature<WayNode>>(getWayNodes(tablePrefix));
		
		// Wrap the way node source into a feature populator that can attach them to their
		// owning ways.
		featurePopulators.add(
				new FeaturePopulatorImpl<Way, WayNode, DbFeature<WayNode>>(
						wayNodeIterator, new WayNodeCollectionLoader()));
		
		return featurePopulators;
	}
}
