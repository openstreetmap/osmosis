// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.lifecycle;

import java.util.Iterator;



/**
 * Defines an iterator that must be explicitly released after use.
 * 
 * @param <T>
 *            The data type to be iterated.
 * @author Brett Henderson
 */
public interface ReleasableIterator<T> extends Iterator<T>, Releasable {
	// This combines multiple interfaces but doesn't add methods.
}
