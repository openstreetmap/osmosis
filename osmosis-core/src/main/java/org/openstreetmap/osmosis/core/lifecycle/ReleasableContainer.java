// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.lifecycle;

import java.util.ArrayList;
import java.util.List;




/**
 * A container for releasable objects that must be freed. This implementation
 * simplifies the creation of many releasable objects that must succeed or fail
 * as a group. As each is created they are added to this container, and after
 * they're all created successfully they can be cleared from this container.
 * 
 * @author Brett Henderson
 */
public class ReleasableContainer implements Closeable {
	private List<Closeable> objects;
	
	
	/**
	 * Creates a new instance.
	 */
	public ReleasableContainer() {
		objects = new ArrayList<Closeable>();
	}
	
	
	/**
	 * Adds a new object to be managed. The object is returned to allow method
	 * chaining.
	 * 
	 * @param <T>
	 *            The type of object being stored.
	 * @param object
	 *            The object to be stored.
	 * @return The object that was stored.
	 */
	public <T extends Closeable> T add(T object) {
		objects.add(object);
		
		return object;
	}
	
	
	/**
	 * Removes all objects. They will no longer be released.
	 */
	public void clear() {
		objects.clear();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		for (Closeable object : objects) {
			object.close();
		}
		objects.clear();
	}
}
