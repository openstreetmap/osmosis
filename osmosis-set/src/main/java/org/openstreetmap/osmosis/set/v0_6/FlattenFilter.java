// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.sort.v0_6.SortedDuplicateEntityPipeValidator;


/**
 * Flatten / simplify a sorted entity stream. (similar to --simplify-change)
 */
public class FlattenFilter extends SortedDuplicateEntityPipeValidator {
	private Sink sink;
	
	private Sink flattener = new Sink() {
		private EntityContainer previousContainer;
	    
	    
		@Override
	    public void initialize(Map<String, Object> metaData) {
			sink.initialize(metaData);
		}
		
		/**
		 * Process a node, way or relation.
		 * 
		 * @param currentContainer
		 *            The entity container to be processed.
		 */
		@Override
		public void process(EntityContainer currentContainer) {
			if (previousContainer == null) {
				previousContainer = currentContainer;
				return;
			}

			Entity current = currentContainer.getEntity();
			Entity previous = previousContainer.getEntity();

			if (current.getId() != previous.getId() || !current.getType().equals(previous.getType())) {
				sink.process(previousContainer);
				previousContainer = currentContainer;
				return;
			}

			if (current.getVersion() > previous.getVersion()) {
				previousContainer = currentContainer;
			}
		}


		@Override
		public void complete() {
			/*
			 * If we've stored entities temporarily, we now need to forward the
			 * stored ones to the output.
			 */
			if (previousContainer != null) {
				sink.process(previousContainer);
			}

			sink.complete();
		}


		@Override
		public void release() {
			sink.release();
		}
	};


	/**
	 * Creates a new instance.
	 */
	public FlattenFilter() {
		super.setSink(flattener);
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
