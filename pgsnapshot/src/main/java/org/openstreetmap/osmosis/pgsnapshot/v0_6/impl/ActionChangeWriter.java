// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;


/**
 * Writes entities to a database according to a specific action.
 * 
 * @author Brett Henderson
 */
public class ActionChangeWriter implements EntityProcessor {
	private ChangeWriter changeWriter;
	private ChangeAction action;
	private boolean keepInvalidWays;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param changeWriter
	 *            The underlying change writer.
	 * @param action
	 *            The action to apply to all writes.
	 * @param keepInvalidWays
	 *            If true, zero and single node ways are kept. Otherwise they are
	 *            silently dropped to avoid putting invalid geometries into the 
	 *            database which can cause problems with postgis functions.
	 */
	public ActionChangeWriter(ChangeWriter changeWriter, ChangeAction action, boolean keepInvalidWays) {
		this.changeWriter = changeWriter;
		this.action = action;
		this.keepInvalidWays = keepInvalidWays;
	}
	
	
	/**
     * {@inheritDoc}
     */
    public void process(BoundContainer bound) {
        // Do nothing.
    }
    
    
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		changeWriter.write(nodeContainer.getEntity(), action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		changeWriter.write(wayContainer.getEntity(), action, keepInvalidWays);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		changeWriter.write(relationContainer.getEntity(), action);
	}
}
