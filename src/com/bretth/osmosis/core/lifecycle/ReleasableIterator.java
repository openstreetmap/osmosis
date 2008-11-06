// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.lifecycle;

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
