// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;


/**
 * Some class implementations persist information and require notification to
 * complete all output prior to being released. In this case, clients of those
 * classes should call the complete method. The complete method is typically
 * only required in cases where the writing class will be released and then a
 * new instance opened on the same data. The complete method is not required in
 * cases where the same object instance will be used to write and read the same
 * information, in that case the instance is expected to be able to persist all
 * information correctly before reading commences.
 * 
 * @author Brett Henderson
 */
public interface Completable extends Releasable {
	
	/**
	 * Ensures that all information is fully persisted. Where the releasable
	 * method of a Releasable class should be called within a finally block,
	 * this method should typically be the final statement within the try block.
	 */
	void complete();
}
