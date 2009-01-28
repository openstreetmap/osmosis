// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;


/**
 * Extends the basic Source interface with the Runnable capability. Runnable
 * is not applied to the Source interface because tasks that act as filters
 * do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableSource extends Source, Runnable {
	// This interface combines Source and Runnable but doesn't introduce
	// methods of its own.
}
