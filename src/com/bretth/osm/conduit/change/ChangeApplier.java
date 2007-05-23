package com.bretth.osm.conduit.change;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.change.impl.ApplierState;
import com.bretth.osm.conduit.change.impl.ApplierBaseInput;
import com.bretth.osm.conduit.change.impl.ApplierChangeInput;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.Sink;
import com.bretth.osm.conduit.task.SinkChangeSinkSource;


/**
 * Applies a change set to an input source and produces an updated data set.
 * 
 * @author Brett Henderson
 */
public class ChangeApplier implements SinkChangeSinkSource {
	
	private ApplierState applierState;
	private ApplierBaseInput applyBaseInput;
	private ApplierChangeInput applyChangeInput;
	
	
	/**
	 * Creates a new instance.
	 */
	public ChangeApplier() {
		applierState = new ApplierState();
		
		applyBaseInput = new ApplierBaseInput(applierState);
		applyChangeInput = new ApplierChangeInput(applierState);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		switch (instance) {
		case 0:
			return applyBaseInput;
		default:
			throw new ConduitRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
	}


	/**
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public ChangeSink getChangeSink(int instance) {
		switch (instance) {
		case 0:
			return applyChangeInput;
		default:
			throw new ConduitRuntimeException("Change sink instance " + instance
					+ " is not valid.");
		}
	}


	/**
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getChangeSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		applierState.setSink(sink);
	}
}
