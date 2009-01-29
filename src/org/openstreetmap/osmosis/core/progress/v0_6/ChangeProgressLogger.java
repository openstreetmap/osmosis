// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.progress.v0_6;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.progress.v0_6.impl.ProgressTracker;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;


/**
 * Logs progress information using jdk logging at info level at regular intervals.
 * 
 * @author Brett Henderson
 */
public class ChangeProgressLogger implements ChangeSinkChangeSource {
	
	private static final Logger log = Logger.getLogger(ChangeProgressLogger.class.getName());
	
	private ChangeSink changeSink;
	private ProgressTracker progressTracker;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public ChangeProgressLogger(int interval) {
		progressTracker = new ProgressTracker(interval);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		Entity entity;
		ChangeAction action;
		
		entity = changeContainer.getEntityContainer().getEntity();
		action = changeContainer.getAction();
		
		if (progressTracker.updateRequired()) {
			log.info(
					"Processing " + entity.getType() + " " + entity.getId() + " with action " + action+ ", "
					+ progressTracker.getObjectsPerSecond() + " objects/second.");
		}
		
		changeSink.process(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		log.info("Processing completion steps.");
		
		changeSink.complete();
		
		log.info("Processing complete.");
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
