// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * Defines the methods to be implemented by data classes stored within an index.
 * <p>
 * Classes implementing this interface provide Storeable functionality with
 * restrictions.
 * <ul>
 * <li>All instances must persist using an identical number of bytes.</li>
 * <li>The key must be persisted first allowing a key instance to be loaded
 * independently.</li>
 * </ul>
 * 
 * @param <K>
 *            The index key type.
 * @author Brett Henderson
 */
public interface IndexElement<K> extends Storeable {
	/**
	 * Returns the key associated with this element for the purposes of
	 * indexing.
	 * 
	 * @return The key of the index element.
	 */
	K getKey();
}
