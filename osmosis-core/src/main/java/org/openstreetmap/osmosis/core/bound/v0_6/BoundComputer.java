// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.bound.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.GenericObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Computes the minimal bounding box of an entity stream.
 * 
 * A bound entity is emitted iff there are nodes on the input stream.
 * 
 * Upstream bound entities are never passed through. If there is a bound entity
 * on the input stream, it is overwritten if there are any nodes or removed if
 * there are no nodes on the input stream.
 * 
 * This implementation caches all objects of the input stream in a simple object
 * store.
 * 
 * @author Igor Podolskiy
 */
public class BoundComputer implements SinkSource, EntityProcessor {

	private Sink sink;
	private SimpleObjectStore<EntityContainer> objects;
	private double top;
	private double bottom;
	private double left;
	private double right;
	private boolean nodesSeen;
	private String origin;


	/**
	 * Creates a new bounding box computer instance.
	 * 
	 * @param origin
	 *            The origin for the bound to set.
	 */

	public BoundComputer(String origin) {
		objects = new SimpleObjectStore<EntityContainer>(new GenericObjectSerializationFactory(), "cbbo", true);
		bottom = 0;
		top = 0;
		left = 0;
		right = 0;
		nodesSeen = false;
		this.origin = origin;
	}


	@Override
	public void initialize(Map<String, Object> metaTags) {
		sink.initialize(metaTags);
	}


	@Override
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}


	@Override
	public void complete() {
		objects.complete();

		if (nodesSeen) {
			sink.process(new BoundContainer(new Bound(right, left, top, bottom, this.origin)));
		}

		ReleasableIterator<EntityContainer> iter = null;

		try {
			iter = objects.iterate();

			while (iter.hasNext()) {
				sink.process(iter.next());
			}
		} finally {
			if (iter != null) {
				iter.release();
			}
		}

		sink.complete();
	}


	@Override
	public void release() {
		sink.release();
		objects.release();
	}


	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}


	@Override
	public void process(BoundContainer bound) {
		// Do nothing, we'll generate a new bound later on
	}


	@Override
	public void process(NodeContainer nodeContainer) {
		Node node = nodeContainer.getEntity();

		if (nodesSeen) {
			left = Math.min(left, node.getLongitude());
			right = Math.max(right, node.getLongitude());

			bottom = Math.min(bottom, node.getLatitude());
			top = Math.max(top, node.getLatitude());
		} else {
			left = node.getLongitude();
			right = node.getLongitude();
			top = node.getLatitude();
			bottom = node.getLatitude();
			nodesSeen = true;
		}

		objects.add(nodeContainer);
	}


	@Override
	public void process(WayContainer way) {
		objects.add(way);
	}


	@Override
	public void process(RelationContainer relation) {
		objects.add(relation);
	}
}
