// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * Translates a change stream into a full history stream which is a normal
 * entity stream with visible attributes.
 * 
 * @author Brett Henderson
 */
public class ChangeToFullHistoryConvertor implements ChangeSinkSource {
	
	private Sink sink;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(Sink sink) {
		this.sink = sink;
	}
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		// Deleted entities are not visible, all others are.
		boolean visible = (ChangeAction.Delete != change.getAction());
		
		// Set a visible meta-tag on the entity because the Osmosis data model
		// doesn't natively support visible.
		EntityContainer entityContainer = change.getEntityContainer().getWriteableInstance();
		entityContainer.getEntity().getMetaTags().put("visible", visible);
		
		sink.process(entityContainer);
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
}
