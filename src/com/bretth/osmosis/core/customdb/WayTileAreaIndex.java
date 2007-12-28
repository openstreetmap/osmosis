package com.bretth.osmosis.core.customdb;

import java.io.File;

import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.store.IndexStore;
import com.bretth.osmosis.core.store.UnsignedIntLongIndexElement;


/**
 * A class for managing a way tile index of varying granularity allowing
 * different sized tiles for different sized ways.
 */
public class WayTileAreaIndex {
	private int[] masks;
	private IndexStore<UnsignedIntLongIndexElement>[] indexes;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param indexFile
	 *            The file to use for storing the index.
	 * @param tileMask
	 *            The mask to be applied to the extremities of a way to see
	 *            if it fits entirely within a tile index of this
	 *            granularity.
	 */
	public WayTileAreaIndex(int tileMask, File indexFile) {
	}
	
	
	/**
	 * Writes the specified element to the index.
	 * 
	 * @param element
	 *            The index element which includes the identifier when stored.
	 */
	public void write(Way way) {
		
	}
}
