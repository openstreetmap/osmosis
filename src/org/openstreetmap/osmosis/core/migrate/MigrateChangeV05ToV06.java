// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.migrate;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.migrate.impl.EntityContainerMigrater;


/**
 * A task for converting 0.5 change data into 0.6 format.  This isn't a true migration but okay for many uses.
 * 
 * @author Brett Henderson
 */
public class MigrateChangeV05ToV06 implements ChangeSink05ChangeSource06 {
	
	private org.openstreetmap.osmosis.core.task.v0_6.ChangeSink changeSink;
	private EntityContainerMigrater migrater;
	
	
	/**
	 * Creates a new instance.
	 */
	public MigrateChangeV05ToV06() {
		migrater = new EntityContainerMigrater();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(org.openstreetmap.osmosis.core.container.v0_5.ChangeContainer changeContainer) {
		changeSink.process(
				new ChangeContainer(
						migrater.migrate(changeContainer.getEntityContainer()),
						changeContainer.getAction()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		changeSink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		changeSink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(org.openstreetmap.osmosis.core.task.v0_6.ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}
