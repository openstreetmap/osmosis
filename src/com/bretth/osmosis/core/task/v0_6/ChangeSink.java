// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_6;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.lifecycle.Completable;
import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for all tasks consuming OSM changes to data.
 * 
 * @author Brett Henderson
 */
public interface ChangeSink extends Task, Completable {
	
	/**
	 * Process the change.
	 * 
	 * @param change
	 *            The change to be processed.
	 */
	public void process(ChangeContainer change);
}
