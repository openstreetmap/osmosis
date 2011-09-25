package org.openstreetmap.osmosis.core.bound.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

public class BoundSetter implements SinkSource {

	private Sink sink;
	private boolean boundProcessed;
	private boolean remove;
	private Bound newBound;
	
	
	public BoundSetter(boolean remove, Bound newBound) {
		this.remove = remove;
		this.newBound = newBound;
		this.boundProcessed = false;
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
			if (remove) {
				// Just returning won't pass the entity downstream
				return;
			} else if (newBound != null) {
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
