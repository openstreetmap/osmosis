// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.lifecycle;


/**
 * Some class implementations persist information and require notification to
 * complete all output prior to being released. In this case, clients of those
 * classes should call the complete method.
 * 
 * @author Brett Henderson
 */
public interface Completable extends Releasable {

	/**
	 * Ensures that all information is fully persisted. This includes database
	 * commits, file buffer flushes, etc. Implementations must call complete on
	 * any nested Completable objects. Where the releasable method of a
	 * Releasable class should be called within a finally block, this method
	 * should typically be the final statement within the try block.
	 */
	void complete();
}
