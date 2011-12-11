// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.task.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.lifecycle.Completable;


/**
 * This interface defines methods required to manage class lifecycles. All
 * clients must first call the initialize method, then the implementation
 * specific processing methods, then complete, and finally the release method.
 * It may be possible to call initialize multiple times, but each call must be
 * matched by a call to complete. Release must be called at the completion of
 * all processing. It may be possible to call release multiple times, but
 * initialize must be called again before processing can proceed.
 * 
 * @author Brett Henderson
 */
public interface Initializable extends Completable {

	/**
	 * Initialize the object. Any global information applicable to this
	 * processing phase can be generically set as meta data.
	 * 
	 * @param metaData
	 *            Meta data applicable to this pipeline invocation.
	 */
	void initialize(Map<String, Object> metaData);
}
