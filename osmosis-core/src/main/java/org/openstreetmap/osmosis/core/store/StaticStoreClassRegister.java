// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;



/**
 * This store class register dynamically allocates identifiers for classes as they are encountered
 * while writing to the store. These identifiers are maintained in memory and used while reading
 * back from the store.
 */
public class StaticStoreClassRegister extends BaseStoreClassRegister {
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param classes
	 *            The classes to be supported by this register.
	 */
	public StaticStoreClassRegister(Class<?>[] classes) {
		super();
		
		byte currentId;
		
		currentId = 0;
		for (Class<?> clazz : classes) {
			registerClass(clazz, currentId++);
		}
	}
}
