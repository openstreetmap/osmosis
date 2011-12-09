// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.bound.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Ensures the bound entity in the output stream exists with a specific value or
 * is not present.
 * 
 * @author Igor Podolskiy
 */
public class BoundSetter implements SinkSource {

	private Sink sink;
	private boolean boundProcessed;
	private Bound newBound;


	/**
	 * Creates a new instance of the bound setter.
	 * 
	 * @param newBound
	 *            the new bound to set, or <pre>null</pre> to remove the bound
	 */
	public BoundSetter(Bound newBound) {
		this.newBound = newBound;
		this.boundProcessed = false;
	}


	@Override
	public void initialize(Map<String, Object> metaTags) {
		sink.initialize(metaTags);
	}


	@Override
	public void process(EntityContainer entityContainer) {
		if (boundProcessed) {
			sink.process(entityContainer);
		} else {
			// processFirstEntity will send all data downstream as needed
			processFirstEntity(entityContainer);
			boundProcessed = true;
		}
	}


	private void processFirstEntity(EntityContainer entityContainer) {
		if (entityContainer.getEntity().getType() == EntityType.Bound) {
			if (newBound == null) {
				// Just returning won't pass the entity downstream
				return;
			} else {
				sink.process(new BoundContainer(newBound));
			}
		} else {
			if (newBound != null) {
				sink.process(new BoundContainer(newBound));
			}
			sink.process(entityContainer);
		}
	}


	@Override
	public void complete() {
		sink.complete();
	}


	@Override
	public void release() {
		sink.release();
	}


	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}

}
