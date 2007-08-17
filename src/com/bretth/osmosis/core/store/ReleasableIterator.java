package com.bretth.osmosis.core.store;

import java.util.Iterator;


/**
 * Defines an iterator that must be explicitly released after use.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public interface ReleasableIterator<DataType> extends Iterator<DataType>, Releasable {
	// This combines multiple interfaces but doesn't add methods.
}
