package com.bretth.osmosis.core.index;

import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * An index element factory implement for long-long index elements.
 * 
 * @author Brett Henderson
 */
public class LongLongElementFactory implements IndexElementFactory<LongLongElement> {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LongLongElement loadElement(StoreReader sr) {
		return new LongLongElement(sr);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeElement(StoreWriter sw, LongLongElement element) {
		element.store(sw);
	}
}
