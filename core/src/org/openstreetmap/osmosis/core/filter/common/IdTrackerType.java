// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.common;

/**
 * Defines the different id tracker implementations available.
 * 
 * @author Brett Henderson
 * 
 */
public enum IdTrackerType {
	/**
	 * The BitSet implementation maintains an array of bits set to 0 or 1. This is the most compact
	 * storage representation for individual ids but is very wasteful when the ids are sparsely
	 * allocated because a bit is allocated for every id in the id range. This should be used if a
	 * large portion of the entire dataset must be stored.
	 */
	BitSet,
	/**
	 * The IdList implementation maintains an array of selected ids. This use 32 bits per id but is
	 * more efficient when a smaller number of ids are being stored. For example, in a bounding box
	 * implementation this implementation will be more efficient if the bounding box contains less
	 * than approximately 1/32 of the entire dataset.
	 */
	IdList,
	/**
	 * The dynamic implementation maintains an array of segments. Each segment manages a fixed range
	 * of ids. Each segment is created on demand. Each segment internally manages the id list with
	 * either one of the two other id list implementations depending on the number of ids to be
	 * managed.
	 */
	Dynamic
}
