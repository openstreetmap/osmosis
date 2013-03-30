// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao extends EntityDao<Way> {
	
	private static final String SQL_UPDATE_WAY_BBOX =
		"UPDATE ways SET bbox = ("
		+ " SELECT ST_Envelope(ST_Collect(geom))"
		+ " FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id"
		+ " WHERE way_nodes.way_id = ways.id"
		+ " )"
		+ " WHERE ways.id = ?";
	private static final String SQL_UPDATE_WAY_LINESTRING =
		"UPDATE ways w SET linestring = ("
		+ " SELECT ST_MakeLine(c.geom) AS way_line FROM ("
		+ " SELECT n.geom AS geom FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id"
		+ " WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id"
		+ " ) c"
		+ " )"
		+ " WHERE w.id  = ?";
	
	private DatabaseCapabilityChecker capabilityChecker;
	private EntityFeatureDao<WayNode, DbOrderedFeature<WayNode>> wayNodeDao;
	private PreparedStatement updateWayBboxStatement;
	private PreparedStatement updateWayLinestringStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public WayDao(DatabaseContext dbCtx, ActionDao actionDao) {
		super(dbCtx, new WayMapper(), actionDao);
		
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
		wayNodeDao = new EntityFeatureDao<WayNode, DbOrderedFeature<WayNode>>(dbCtx, new WayNodeMapper());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadFeatures(long entityId, Way entity) {
		entity.getWayNodes().addAll(wayNodeDao.getAllRaw(entityId));
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
			if (updateWayBboxStatement == null) {
				updateWayBboxStatement = prepareStatement(SQL_UPDATE_WAY_BBOX);
			}
			
			try {
				int prmIndex;
				
				prmIndex = 1;
				updateWayBboxStatement.setLong(prmIndex++, wayId);
				updateWayBboxStatement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Update bbox failed for way " + wayId + ".");
			}
		}
		if (capabilityChecker.isWayLinestringSupported()) {
			if (updateWayLinestringStatement == null) {
				updateWayLinestringStatement = prepareStatement(SQL_UPDATE_WAY_LINESTRING);
			}
			
			try {
				int prmIndex;
				
				prmIndex = 1;
				updateWayLinestringStatement.setLong(prmIndex++, wayId);
				updateWayLinestringStatement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Update linestring failed for way " + wayId + ".");
			}
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


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Way> iterate() {
		return new WayReader(getDatabaseContext());
	}
}
