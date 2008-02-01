// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;


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
	 * Releases resources held by the object. The implementation must not throw
	 * exceptions. The method may be called multiple times. This should be
	 * called within a finally block whenever this object is not required any
	 * more.
	 */
	public void release();
}
