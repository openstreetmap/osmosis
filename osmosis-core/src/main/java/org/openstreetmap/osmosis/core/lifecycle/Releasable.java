// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.lifecycle;


/**
 * Classes that hold heavyweight resources that can't wait for garbage
 * collection should implement this interface. It provides a release method that
 * should be called by all clients when the class is no longer required. This
 * release method is guaranteed not to throw exceptions and should always be
 * called within a finally clause.
 * 
 * @author Brett Henderson
 */
public interface Releasable {
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete.
	 * Implementations should support calling release multiple times, however
	 * this is not mandatory and cannot be relied on by clients. Implementations
	 * must call release on any nested Releasable objects. It does not throw
	 * exceptions and should be called within a finally block to ensure it is
	 * called in exception scenarios.
	 */
	void release();
}
