// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.set.v0_6.impl.ChangeSimplifierImpl;
import org.openstreetmap.osmosis.core.sort.v0_6.SortedHistoryChangePipeValidator;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSinkChangeSource;


/**
 * Looks at a sorted change stream and condenses multiple changes for a single entity into a single
 * change.
 * 
 * @author Brett Henderson
 */
public class ChangeSimplifier implements ChangeSinkChangeSource {

	private SortedHistoryChangePipeValidator orderingValidator;
	private ChangeSimplifierImpl changeSimplifier;
	
	
	/**
	 * Creates a new instance.
	 */
	public ChangeSimplifier() {
		orderingValidator = new SortedHistoryChangePipeValidator();
		changeSimplifier = new ChangeSimplifierImpl();
		
		orderingValidator.setChangeSink(changeSimplifier);
	}
    
    
    /**
     * {@inheritDoc}
     */
	@Override
    public void initialize(Map<String, Object> metaData) {
		orderingValidator.initialize(metaData);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(ChangeContainer change) {
		orderingValidator.process(change);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		orderingValidator.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		orderingValidator.release();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		changeSimplifier.setChangeSink(changeSink);
	}

}
