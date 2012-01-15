// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.progress.v0_6;

import java.util.Map;
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
	
	private static final Logger LOG = Logger.getLogger(ChangeProgressLogger.class.getName());
	
	private ChangeSink changeSink;
	private ProgressTracker progressTracker;
	
	private String prefix;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 * @param label
	 *            a label to prefix the logger with; may be null.
	 */
	public ChangeProgressLogger(int interval, String label) {
		progressTracker = new ProgressTracker(interval);

		if (label != null && !label.equals("")) {
			prefix = "[" + label + "] ";
		} else {
			prefix = "";
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void initialize(Map<String, Object> metaData) {
		progressTracker.initialize();
		changeSink.initialize(metaData);
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
			LOG.info(
					prefix 
					+ "Processing " + entity.getType() + " " + entity.getId() + " with action " + action + ", "
					+ progressTracker.getObjectsPerSecond() + " objects/second.");
		}
		
		changeSink.process(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		LOG.info("Processing completion steps.");
		
		long start = System.currentTimeMillis();
		changeSink.complete();
		long duration = System.currentTimeMillis() - start;
		
		LOG.info("Completion steps took " + duration / 1000d + " seconds.");
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
