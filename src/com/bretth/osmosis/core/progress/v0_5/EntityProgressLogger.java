// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.progress.v0_5;

import java.util.Date;
import java.util.logging.Logger;

import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.domain.v0_5.Entity;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkSource;


/**
 * Logs progress information using jdk logging at info level at regular intervals.
 * 
 * @author Brett Henderson
 */
public class EntityProgressLogger implements SinkSource {
	
	private static final Logger log = Logger.getLogger(EntityProgressLogger.class.getName());
	
	private Sink sink;
	private int interval;
	private boolean initialized;
	private Date lastUpdateTimestamp;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public EntityProgressLogger(int interval) {
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
	public void process(EntityContainer entityContainer) {
		Entity entity;
		
		entity = entityContainer.getEntity();
		
		if (updateRequired()) {
			log.info("Processing " + entity.getType() + " " + entity.getId() + ".");
		}
		
		sink.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		log.info("Processing completion steps.");
		
		sink.complete();
		
		log.info("Processing complete.");
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
