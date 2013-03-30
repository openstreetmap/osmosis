// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Reads all nodes from a database ordered by their identifier. It combines the
 * output of the node table readers to produce fully configured node objects.
 * 
 * @author Brett Henderson
 */
public class NodeReader extends EntityReader<Node> {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public NodeReader(DatabaseContext dbCtx) {
		super(dbCtx, new NodeMapper());
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
	public NodeReader(DatabaseContext dbCtx, String constraintTable) {
		super(dbCtx, new NodeMapper(), constraintTable);
	}
}
