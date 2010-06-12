// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;


/**
 * Extends the basic DatasetSource interface with the Runnable capability.
 * Runnable is not applied to the DatasetSource interface because tasks that act
 * as filters do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableDatasetSource extends DatasetSource, Runnable {
	// This interface combines DatasetSource and Runnable but doesn't introduce
	// methods of its own.
}
