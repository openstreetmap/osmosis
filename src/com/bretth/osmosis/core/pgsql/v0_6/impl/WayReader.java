// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBWayNode;
import com.bretth.osmosis.core.mysql.v0_6.impl.WayNodeComparator;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;


/**
 * Reads all ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class WayReader extends EntityReader<Way> {
	
	private PeekableIterator<DBWayNode> wayNodeReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public WayReader(DatabaseContext dbCtx) {
		super(dbCtx, new WayBuilder());
		
		wayNodeReader = new PeekableIterator<DBWayNode>(
			new EntityFeatureTableReader<WayNode, DBWayNode>(dbCtx, new WayNodeBuilder())
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
		super(dbCtx, new WayBuilder(), constraintTable);
		
		wayNodeReader = new PeekableIterator<DBWayNode>(
			new EntityFeatureTableReader<WayNode, DBWayNode>(dbCtx, new WayNodeBuilder(), constraintTable)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateEntityFeatures(Way entity) {
		long wayId;
		List<DBWayNode> wayNodes;
		
		super.populateEntityFeatures(entity);
		
		wayId = entity.getId();
		
		// Skip all way nodes that are from a lower way.
		while (wayNodeReader.hasNext()) {
			DBWayNode wayNode;
			
			wayNode = wayNodeReader.peekNext();
			
			if (wayNode.getEntityId() < wayId) {
				wayNodeReader.next();
			} else {
				break;
			}
		}
		
		// Load all nodes matching this version of the way.
		wayNodes = new ArrayList<DBWayNode>();
		while (wayNodeReader.hasNext() && wayNodeReader.peekNext().getEntityId() == wayId) {
			wayNodes.add(wayNodeReader.next());
		}
		// The underlying query sorts node references by way id but not
		// by their sequence number.
		Collections.sort(wayNodes, new WayNodeComparator());
		for (DBWayNode dbWayNode : wayNodes) {
			entity.addWayNode(dbWayNode.getFeature());
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
