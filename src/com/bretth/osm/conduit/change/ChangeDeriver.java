package com.bretth.osm.conduit.change;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.change.impl.DeriverFromInput;
import com.bretth.osm.conduit.change.impl.DeriverToInput;
import com.bretth.osm.conduit.change.impl.DeriverState;
import com.bretth.osm.conduit.task.ChangeSink;
import com.bretth.osm.conduit.task.MultiSinkChangeSource;
import com.bretth.osm.conduit.task.Sink;


/**
 * Compares two different data sources and produces a set of differences.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriver implements MultiSinkChangeSource {

	private DeriverState comparisonState;

	private Sink fromInput;

	private Sink toInput; 


	/**
	 * Creates a new instance.
	 */
	public ChangeDeriver() {
		comparisonState = new DeriverState();
		
		fromInput = new DeriverFromInput(comparisonState);
		toInput = new DeriverToInput(comparisonState);
	}


	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		switch (instance) {
		case 0:
			return fromInput;
		case 1:
			return toInput;
		default:
			throw new ConduitRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
	}


	/**
	 * This implementation always returns 2.
	 * 
	 * @return 2
	 */
	public int getSinkCount() {
		return 2;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		comparisonState.setChangeSink(changeSink);
	}

}
