package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * An index element factory implement for int-long index elements.
 * 
 * @author Brett Henderson
 */
public class IntLongElementFactory implements IndexElementFactory<IntLongElement> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IntLongElement loadElement(StoreReader sr) {
		return new IntLongElement(sr);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeElement(StoreWriter sw, IntLongElement element) {
		element.store(sw);
	}
}
