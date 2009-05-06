// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_6;


/**
 * Allows entire datasets to be passed between tasks. Tasks supporting this data
 * type are expected to perform operations that require random access to an
 * entire data set which cannot be performed in a "streamy" fashion.
 * 
 * @author Brett Henderson
 */
public interface Dataset {
	
	/**
	 * Creates a new reader instance providing access to the data within this
	 * set.
	 * 
	 * @return A new dataset reader.
	 */
	DatasetContext createReader();
}
