// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Stores the results for a decoded Blob.
 * 
 * @author Brett Henderson
 */
public class PbfBlobResult {
	private List<EntityContainer> entities;
	private boolean complete;
	private boolean success;


	/**
	 * Creates a new instance.
	 */
	public PbfBlobResult() {
		complete = false;
		success = false;
	}


	/**
	 * Stores the results of a successful blob decoding operation.
	 * 
	 * @param decodedEntities
	 *            The entities from the blob.
	 */
	public void storeSuccessResult(List<EntityContainer> decodedEntities) {
		entities = decodedEntities;
		complete = true;
		success = true;
	}


	/**
	 * Stores a failure result for a blob decoding operation.
	 */
	public void storeFailureResult() {
		complete = true;
		success = false;
	}


	/**
	 * Gets the complete flag.
	 * 
	 * @return True if complete.
	 */
	public boolean isComplete() {
		return complete;
	}


	/**
	 * Gets the success flag. This is only valid after complete becomes true.
	 * 
	 * @return True if successful.
	 */
	public boolean isSuccess() {
		return success;
	}


	/**
	 * Gets the entities decoded from the blob. This is only valid after
	 * complete becomes true, and if success is true.
	 * 
	 * @return The list of decoded entities.
	 */
	public List<EntityContainer> getEntities() {
		return entities;
	}
}
