package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.task.Sink;


/**
 * Provides common functionality across input sources to a data comparison.
 * 
 * @author Brett Henderson
 */
public abstract class DeriverInput extends BaseInput implements Sink {
	/**
	 * Maintains the shared state used by multiple input sources during
	 * comparison.
	 */
	protected DeriverState sharedInputState;


	/**
	 * Creates a new instance.
	 * 
	 * @param comparisonState
	 *            The shared state used by multiple input sources during
	 *            comparison.
	 */
	protected DeriverInput(DeriverState comparisonState) {
		this.sharedInputState = comparisonState;
	}
	
	
	/**
	 * Checks to ensure that input sources have not encountered errors.
	 */
	protected void validateNoErrors() {
		// Check if the "from" source has been released without completing.
		if (sharedInputState.fromReleased && sharedInputState.fromStatus != InputStatus.COMPLETE) {
			throw new ConduitRuntimeException("The comparison \"from\" source has encountered an error.");
		}
		
		// Check if the "to" source has been released without completing.
		if (sharedInputState.toReleased && sharedInputState.toStatus != InputStatus.COMPLETE) {
			throw new ConduitRuntimeException("The comparison \"to\" source has encountered an error.");
		}
	}
}
