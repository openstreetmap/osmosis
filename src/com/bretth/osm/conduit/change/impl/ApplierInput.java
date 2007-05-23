package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.ConduitRuntimeException;


/**
 * Provides common functionality across input sources to a data modification.
 * 
 * @author Brett Henderson
 */
public abstract class ApplierInput extends BaseInput {
	/**
	 * Maintains the shared state used by multiple input sources during
	 * comparison.
	 */
	protected ApplierState sharedInputState;


	/**
	 * Creates a new instance.
	 * 
	 * @param applierState
	 *            The shared state used by multiple input sources during
	 *            change application.
	 */
	protected ApplierInput(ApplierState applierState) {
		this.sharedInputState = applierState;
	}
	
	
	/**
	 * Checks to ensure that input sources have not encountered errors.
	 */
	protected void validateNoErrors() {
		// Check if the "base" source has been released without completing.
		if (sharedInputState.baseReleased && sharedInputState.baseStatus != InputStatus.COMPLETE) {
			throw new ConduitRuntimeException("The comparison \"base\" source has encountered an error.");
		}
		
		// Check if the "change" source has been released without completing.
		if (sharedInputState.changeReleased && sharedInputState.changeStatus != InputStatus.COMPLETE) {
			throw new ConduitRuntimeException("The comparison \"change\" source has encountered an error.");
		}
	}
}
