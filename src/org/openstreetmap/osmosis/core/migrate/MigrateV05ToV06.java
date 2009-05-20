// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.migrate;

import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.migrate.impl.EntityContainerMigrater;


/**
 * A task for converting 0.5 data into 0.6 format.  This isn't a true migration but okay for most uses.
 * 
 * @author Brett Henderson
 */
public class MigrateV05ToV06 implements Sink05Source06, EntityProcessor {
	
	private org.openstreetmap.osmosis.core.task.v0_6.Sink sink;
	private EntityContainerMigrater migrater;
	
	
	/**
	 * Creates a new instance.
	 */
	public MigrateV05ToV06() {
		migrater = new EntityContainerMigrater();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void process(BoundContainer bound) {
		sink.process(migrater.migrate(bound));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void process(NodeContainer node) {
		sink.process(migrater.migrate(node));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void process(WayContainer way) {
		sink.process(migrater.migrate(way));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void process(RelationContainer relation) {
		sink.process(migrater.migrate(relation));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void complete() {
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
	
	public void setSink(org.openstreetmap.osmosis.core.task.v0_6.Sink sink) {
		this.sink = sink;
	}
}
