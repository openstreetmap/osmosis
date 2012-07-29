// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Performs all node-specific db operations.
 * 
 * @author Brett Henderson
 */
public class NodeDao extends EntityDao<Node> {
	private static final String SQL_UPDATE_WAY_BBOX =
		"UPDATE ways w SET bbox = ("
		+ " SELECT ST_Envelope(ST_Collect(n.geom))"
		+ " FROM nodes n INNER JOIN way_nodes wn ON wn.node_id = n.id"
		+ " WHERE wn.way_id = w.id"
		+ " )"
		+ " WHERE w.id IN ("
		+ " SELECT w.id FROM ways w INNER JOIN way_nodes wn ON w.id = wn.way_id WHERE wn.node_id = ? GROUP BY w.id"
		+ " )";
	private static final String SQL_UPDATE_WAY_LINESTRING =
		"UPDATE ways w SET linestring = ("
		+ " SELECT ST_MakeLine(c.geom) AS way_line FROM ("
		+ " SELECT n.geom AS geom FROM nodes n INNER JOIN way_nodes wn ON n.id = wn.node_id"
		+ " WHERE (wn.way_id = w.id) ORDER BY wn.sequence_id"
		+ " ) c"
		+ " )"
		+ " WHERE w.id IN ("
		+ " SELECT w.id FROM ways w INNER JOIN way_nodes wn ON w.id = wn.way_id WHERE wn.node_id = ? GROUP BY w.id"
		+ " )";
	
	
	private DatabaseCapabilityChecker capabilityChecker;
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
	public NodeDao(DatabaseContext dbCtx, ActionDao actionDao) {
		super(dbCtx, new NodeMapper(), actionDao);
		
		capabilityChecker = new DatabaseCapabilityChecker(dbCtx);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadFeatures(long entityId, Node entity) {
		// Nodes have no additional features.
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Node entity) {
		super.modifyEntity(entity);
		
		if (capabilityChecker.isWayBboxSupported()) {
			if (updateWayBboxStatement == null) {
				updateWayBboxStatement = prepareStatement(SQL_UPDATE_WAY_BBOX);
			}
			
			try {
				int prmIndex;
				
				prmIndex = 1;
				updateWayBboxStatement.setLong(prmIndex++, entity.getId());
				updateWayBboxStatement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Update bbox failed for node " + entity.getId() + ".");
			}
		}
		
		if (capabilityChecker.isWayLinestringSupported()) {
			if (updateWayLinestringStatement == null) {
				updateWayLinestringStatement = prepareStatement(SQL_UPDATE_WAY_LINESTRING);
			}
			
			try {
				int prmIndex;
				
				prmIndex = 1;
				updateWayLinestringStatement.setLong(prmIndex++, entity.getId());
				updateWayLinestringStatement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException("Update linestring failed for node " + entity.getId() + ".");
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Node> iterate() {
		return new NodeReader(getDatabaseContext());
	}
}
