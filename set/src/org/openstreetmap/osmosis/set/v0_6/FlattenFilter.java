/* This software is released into the Public Domain.
 * See copying.txt for details.  */
package org.openstreetmap.osmosis.set.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Flatten / simplify a sorted entity stream.
 * (similar to --simplify-change)
 */
public class FlattenFilter implements SinkSource {
	private Sink sink;
	private EntityContainer previous_container;

	/**
	 * Creates a new instance.
	 */
	public FlattenFilter() {
	}

	/**
	 * Process a node, way or relation.
	 *
	 * @param current_container
	 *            The entity container to be processed.
	 */
	public void process(EntityContainer current_container) {
		if (previous_container == null) {
			previous_container = current_container;
			return;
		}

		Entity current = current_container.getEntity();
		Entity previous = previous_container.getEntity();

		if (current.getId() != previous.getId() || !current.getType().equals(previous.getType())) {
			sink.process(previous_container);
			previous_container = current_container;
			return;
		}

		if (current.getVersion() > previous.getVersion())
			previous_container = current_container;
	}

	/**
	 * Process the bound.
	 *
	 * @param boundContainer
	 *            The bound to be processed.
	 */
	public void process(BoundContainer boundContainer) {
		/* By default, pass it on unchanged */
		sink.process(boundContainer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		/*
		 * If we've stored entities temporarily, we now need to
		 * forward the stored ones to the output.
		 */
		if (previous_container != null)
			sink.process(previous_container);

		sink.complete();
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
