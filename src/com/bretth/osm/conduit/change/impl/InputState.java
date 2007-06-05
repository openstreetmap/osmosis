package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.OsmElement;


/**
 * Used by deriver input sources to access shared state between inputs.
 * 
 * @author Brett Henderson
 */
public interface InputState {
	
	/**
	 * Gets the source status for this input.
	 * 
	 * @return The status.
	 */
	public InputStatus getThisSourceStatus();
	
	
	/**
	 * Gets the source status for the input being compared against.
	 * 
	 * @return The status
	 */
	public InputStatus getComparisonSourceStatus();
	
	
	/**
	 * Gets the current element being processed by this source.
	 * 
	 * @return The current element.
	 */
	public OsmElement getThisSourceElement();
	
	
	/**
	 * Gets the current element being processed by the source being compared
	 * against.
	 * 
	 * @return The current element of the comparison source.
	 */
	public OsmElement getComparisonSourceElement();
}
