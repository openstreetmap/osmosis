// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.osmosis.core.database.DbOrderedFeature;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Reads all ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class WayReader extends EntityReader<Way> {
	
	private PeekableIterator<DbOrderedFeature<WayNode>> wayNodeReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public WayReader(DatabaseContext dbCtx) {
		super(dbCtx, new WayMapper());
		
		wayNodeReader = new PeekableIterator<DbOrderedFeature<WayNode>>(
			new EntityFeatureTableReader<WayNode, DbOrderedFeature<WayNode>>(dbCtx, new WayNodeMapper())
		);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param constraintTable
	 *            The table containing a column named id defining the list of
	 *            entities to be returned.
	 */
	public WayReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx, new WayMapper(), constraintTable);
		
		wayNodeReader = new PeekableIterator<DbOrderedFeature<WayNode>>(
			new EntityFeatureTableReader<WayNode, DbOrderedFeature<WayNode>>(
					dbCtx, new WayNodeMapper(), constraintTable)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateEntityFeatures(Way entity) {
		long wayId;
		List<DbOrderedFeature<WayNode>> wayNodes;
		
		super.populateEntityFeatures(entity);
		
		wayId = entity.getId();
		
		// Skip all way nodes that are from a lower way.
		while (wayNodeReader.hasNext()) {
			DbOrderedFeature<WayNode> wayNode;
			
			wayNode = wayNodeReader.peekNext();
			
			if (wayNode.getEntityId() < wayId) {
				wayNodeReader.next();
			} else {
				break;
			}
		}
		
		// Load all nodes matching this version of the way.
		wayNodes = new ArrayList<DbOrderedFeature<WayNode>>();
		while (wayNodeReader.hasNext() && wayNodeReader.peekNext().getEntityId() == wayId) {
			wayNodes.add(wayNodeReader.next());
		}
		// The underlying query sorts node references by way id but not
		// by their sequence number.
		Collections.sort(wayNodes, new DbOrderedFeatureComparator<WayNode>());
		for (DbOrderedFeature<WayNode> dbWayNode : wayNodes) {
			entity.getWayNodes().add(dbWayNode.getFeature());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		super.release();
		
		wayNodeReader.release();
	}
}
