package com.bretth.osmosis.core.migrate;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.migrate.impl.EntityContainerMigrater;


/**
 * A task for converting 0.5 change data into 0.6 format.  This isn't a true migration but okay for many uses.
 * 
 * @author Brett Henderson
 */
public class MigrateChangeV05ToV06 implements ChangeSink05ChangeSource06 {
	
	private com.bretth.osmosis.core.task.v0_6.ChangeSink sink;
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
	public void process(com.bretth.osmosis.core.container.v0_5.ChangeContainer changeContainer) {
		sink.process(new ChangeContainer(migrater.migrate(changeContainer.getEntityContainer()), changeContainer.getAction()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(com.bretth.osmosis.core.task.v0_6.ChangeSink sink) {
		this.sink = sink;
	}
}
