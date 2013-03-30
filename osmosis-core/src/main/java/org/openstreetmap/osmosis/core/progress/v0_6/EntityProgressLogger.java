// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.progress.v0_6;

import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.progress.v0_6.impl.ProgressTracker;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Logs progress information using jdk logging at info level at regular intervals.
 * 
 * @author Brett Henderson
 */
public class EntityProgressLogger implements SinkSource {
	
	private static final Logger LOG = Logger.getLogger(EntityProgressLogger.class.getName());
	
	private Sink sink;
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
	public EntityProgressLogger(int interval, String label) {
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
		sink.initialize(metaData);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		Entity entity;
		
		entity = entityContainer.getEntity();
		
		if (progressTracker.updateRequired()) {
			LOG.info(
					prefix
					+ "Processing " + entity.getType() + " " + entity.getId() + ", "
					+ progressTracker.getObjectsPerSecond() + " objects/second.");
		}
		
		sink.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		LOG.info("Processing completion steps.");
		
		long start = System.currentTimeMillis();
		sink.complete();
		long duration = System.currentTimeMillis() - start;
		
		LOG.info("Completion steps took " + duration / 1000d + " seconds.");
		LOG.info("Processing complete.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
