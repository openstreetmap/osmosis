// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.lifecycle;

/**
 * Classes that hold heavyweight resources that can't wait for garbage
 * collection should implement this interface. It provides a {@link #close()}
 * method that should be called by all clients when the class is no longer
 * required. This release method is guaranteed not to throw exceptions and
 * should always be called within a finally, or try-with-resources clause.
 * 
 * @author Brett Henderson
 */
public interface Closeable extends AutoCloseable {
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete.
	 * Implementations should support being called multiple times, however this
	 * is not mandatory and cannot be relied on by clients. Implementations must
	 * call close on any nested {@link Closeable} objects. It does not throw
	 * exceptions and should be called within a finally, or try-with-resources
	 * clause to ensure it is called in exception scenarios.
	 */
	void close();
}
