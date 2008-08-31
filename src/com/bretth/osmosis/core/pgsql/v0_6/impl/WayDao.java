// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBWayNode;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Performs all way-specific db operations.
 * 
 * @author Brett Henderson
 */
public class WayDao extends EntityDao<Way> {
	
	private static final String SQL_UPDATE_WAY_BBOX =
		"UPDATE ways SET bbox = (" +
		" SELECT Envelope(Collect(geom))" +
		" FROM nodes JOIN way_nodes ON way_nodes.node_id = nodes.id" +
		" WHERE way_nodes.way_id = ways.id" +
		" )" +
		" WHERE ways.id = ?";
	
	
	private EntityFeatureDao<WayNode, DBWayNode> wayNodeDao;
	private PreparedStatement updateWayBboxStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public WayDao(DatabaseContext dbCtx) {
		super(dbCtx, new WayBuilder());
		
		wayNodeDao = new EntityFeatureDao<WayNode, DBWayNode>(dbCtx, new WayNodeBuilder());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getEntity(long entityId) {
		Way way;
		
		way = super.getEntity(entityId);
		
		way.addWayNodes(wayNodeDao.getRawList(entityId));
		
		return way;
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
		List<DBWayNode> dbList;
		
		dbList = new ArrayList<DBWayNode>(wayNodeList.size());
		
		for (int i = 0; i < wayNodeList.size(); i++) {
			dbList.add(new DBWayNode(entityId, wayNodeList.get(i), i));
		}
		
		wayNodeDao.addList(dbList);
	}
	
	
	/**
	 * Updates the bounding box column for the specified way.
	 * 
	 * @param wayId
	 *            The way bounding box.
	 */
	private void updateWayBBox(long wayId) {
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
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Way entity) {
		super.addEntity(entity);
		
		addWayNodeList(entity.getId(), entity.getWayNodeList());
		
		updateWayBBox(entity.getId());
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
		addWayNodeList(entity.getId(), entity.getWayNodeList());
		
		updateWayBBox(entity.getId());
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
