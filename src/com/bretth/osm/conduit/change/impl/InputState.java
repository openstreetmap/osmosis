package com.bretth.osm.conduit.change.impl;

import com.bretth.osm.conduit.data.Element;


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
	 * Gets the source status for the other input.
	 * 
	 * @return The status.
	 */
	public InputStatus getComparisonSourceStatus();
	
	
	/**
	 * Gets the current element being processed by this source.
	 * 
	 * @return The current element.
	 */
	public Element getThisSourceElement();
	
	
	/**
	 * Gets the current element being processed by the other source.
	 * 
	 * @return The current element of the other source.
	 */
	public Element getComparisonSourceElement();
	
	
	/**
	 * Ensures that no errors have occurred on the other input.
	 */
	public void checkForErrors();
}
