// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;


/**
 * Looks at a sorted change stream and condenses multiple changes for a single entity into a single
 * change.
 * 
 * @author Brett Henderson
 */
public class ChangeSimplifierImpl implements ChangeSinkChangeSource {

	private List<ChangeContainer> currentChanges;
	private ChangeSink changeSink;
	
	
	/**
	 * Creates a new instance.
	 */
	public ChangeSimplifierImpl() {
		currentChanges = new ArrayList<ChangeContainer>();
	}
	
	
	private void flushCurrentChanges() {
		ChangeContainer changeBegin;
		ChangeContainer changeEnd;
		ChangeAction actionBegin;
		ChangeAction actionEnd;
		ChangeAction actionResult;
		
		changeBegin = currentChanges.get(0);
		changeEnd = currentChanges.get(currentChanges.size() - 1);
		
		actionBegin = changeBegin.getAction();
		actionEnd = changeEnd.getAction();
		
		// If the final action is a delete we'll send a delete action regardless of whether the
		// first action was a create just in case the create should have been a modify.
		// If the first action is a create, then the result is always a create (except for delete
		// case above).
		// Everything else is treated as a modify.
		if (actionEnd.equals(ChangeAction.Delete)) {
			actionResult = ChangeAction.Delete;
		} else if (actionBegin.equals(ChangeAction.Create)) {
			actionResult = ChangeAction.Create;
		} else {
			actionResult = ChangeAction.Modify;
		}
		
		changeSink.process(new ChangeContainer(changeEnd.getEntityContainer(), actionResult));
		
		currentChanges.clear();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public void initialize(Map<String, Object> metaData) {
		changeSink.initialize(metaData);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		// If the current change is for a different entity to those in our current changes list,
		// then we must process the current changes.
		if (currentChanges.size() > 0) {
			long currentId;
			
			currentId = currentChanges.get(0).getEntityContainer().getEntity().getId();
			
			if (currentId != change.getEntityContainer().getEntity().getId()) {
				flushCurrentChanges();
			}
		}
		
		currentChanges.add(change);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		if (!currentChanges.isEmpty()) {
			flushCurrentChanges();
		}
		
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
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
}
