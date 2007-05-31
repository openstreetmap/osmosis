package com.bretth.osm.conduit.task;


/**
 * Extends the basic ChangeSource interface with the Runnable capability.
 * Runnable is not applied to the ChangeSource interface because tasks that act
 * as filters do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableChangeSource extends ChangeSource, Runnable {
	// This interface combines ChangeSource and Runnable but doesn't introduce
	// methods of its own.
}
