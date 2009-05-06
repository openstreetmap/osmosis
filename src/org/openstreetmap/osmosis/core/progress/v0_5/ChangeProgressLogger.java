// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.progress.v0_5;

import java.util.Date;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_5.ChangeContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.Entity;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSinkChangeSource;


/**
 * Logs progress information using jdk logging at info level at regular intervals.
 * 
 * @author Brett Henderson
 */
public class ChangeProgressLogger implements ChangeSinkChangeSource {
	
	private static final Logger LOG = Logger.getLogger(ChangeProgressLogger.class.getName());
	
	private ChangeSink changeSink;
	private int interval;
	private boolean initialized;
	private Date lastUpdateTimestamp;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public ChangeProgressLogger(int interval) {
		this.interval = interval;
		
		initialized = false;
	}
	
	
	/**
	 * Initialises internal state.  This can be called multiple times.
	 */
	private boolean updateRequired() {
		if (!initialized) {
			lastUpdateTimestamp = new Date();
			
			initialized = true;
			
			return false;
			
		} else {
			Date currentTimestamp;
			long duration;
			
			// Calculate the time since the last update.
			currentTimestamp = new Date();
			duration = currentTimestamp.getTime() - lastUpdateTimestamp.getTime();
			
			if (duration > interval || duration < 0) {
				lastUpdateTimestamp = currentTimestamp;
				
				return true;
				
			} else {
				return false;
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		Entity entity;
		ChangeAction action;
		
		entity = changeContainer.getEntityContainer().getEntity();
		action = changeContainer.getAction();
		
		if (updateRequired()) {
			LOG.info("Processing " + entity.getType() + " " + entity.getId() + " with action " + action + ".");
		}
		
		changeSink.process(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		LOG.info("Processing completion steps.");
		
		changeSink.complete();
		
		LOG.info("Processing complete.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeSink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}
