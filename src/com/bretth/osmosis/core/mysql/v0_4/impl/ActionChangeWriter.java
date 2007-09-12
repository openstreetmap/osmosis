package com.bretth.osmosis.core.mysql.v0_4.impl;

import com.bretth.osmosis.core.container.v0_4.EntityProcessor;
import com.bretth.osmosis.core.container.v0_4.NodeContainer;
import com.bretth.osmosis.core.container.v0_4.SegmentContainer;
import com.bretth.osmosis.core.container.v0_4.WayContainer;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Writes entities to a database according to a specific action.
 * 
 * @author Brett Henderson
 */
public class ActionChangeWriter implements EntityProcessor {
	private ChangeWriter changeWriter;
	private ChangeAction action;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param changeWriter
	 *            The underlying change writer.
	 * @param action
	 *            The action to apply to all writes.
	 */
	public ActionChangeWriter(ChangeWriter changeWriter, ChangeAction action) {
		this.changeWriter = changeWriter;
		this.action = action;
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
	public void process(SegmentContainer segmentContainer) {
		changeWriter.write(segmentContainer.getEntity(), action);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		changeWriter.write(wayContainer.getEntity(), action);
	}
}
