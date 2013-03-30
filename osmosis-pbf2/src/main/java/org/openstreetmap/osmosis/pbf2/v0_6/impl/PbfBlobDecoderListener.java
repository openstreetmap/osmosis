// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Instances of this interface are used to receive results from PBFBlobDecoder.
 * 
 * @author Brett Henderson
 */
public interface PbfBlobDecoderListener {
	/**
	 * Provides the listener with the list of decoded entities.
	 * 
	 * @param decodedEntities
	 *            The decoded entities.
	 */
	void complete(List<EntityContainer> decodedEntities);


	/**
	 * Notifies the listener that an error occurred during processing.
	 */
	void error();
}
