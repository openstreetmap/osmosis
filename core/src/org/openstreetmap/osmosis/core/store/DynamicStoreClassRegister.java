// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;



/**
 * This store class register dynamically allocates identifiers for classes as they are encountered
 * while writing to the store. These identifiers are maintained in memory and used while reading
 * back from the store.
 */
public class DynamicStoreClassRegister extends BaseStoreClassRegister {
	
	private byte idSequence;
	
	
	/**
	 * Creates a new instance.
	 */
	public DynamicStoreClassRegister() {
		super();
		
		idSequence = 0;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeIdentifierForClass(StoreWriter storeWriter, Class<?> clazz) {
		if (!isClassRecognized(clazz)) {
			byte id;
			
			id = idSequence++;
			
			registerClass(clazz, id);
		}
		
		super.storeIdentifierForClass(storeWriter, clazz);
	}
}
