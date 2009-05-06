// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides a cache mapping between classes and storeable constructors. A
 * storeable constructor is a constructor accepting a StoreReader and
 * StoreClassRegister as arguments. This class uses reflection to obtain the
 * required constructor and is suitable for validating classes being written to
 * a store as well as obtaining suitable constructors when reading from a store.
 * 
 * @author Brett Henderson
 */
public class StoreableConstructorCache {
	private Map<Class<?>, Constructor<?>> cache;
	
	
	/**
	 * Creates a new instance.
	 */
	public StoreableConstructorCache() {
		cache = new HashMap<Class<?>, Constructor<?>>();
	}
	
	
	/**
	 * Returns the constructor on the specified class that is used for loading
	 * state from a data store.
	 * 
	 * @param clazz
	 *            The class with the storeable constructor.
	 * @return The storeable class constructor.
	 */
	public Constructor<?> getStoreableConstructor(Class<?> clazz) {
		Constructor<?> constructor;
		
		if (cache.containsKey(clazz)) {
			constructor = cache.get(clazz);
		} else {
			try {
				constructor = clazz.getConstructor(new Class [] {StoreReader.class, StoreClassRegister.class});
				
			} catch (NoSuchMethodException e) {
				throw new OsmosisRuntimeException(
						"Class " + clazz.getName() + " does not have a constructor accepting a "
						+ StoreReader.class.getName() + " argument, this is required for all Storeable classes.", e);
			}
			
			cache.put(clazz, constructor);
		}
		
		return constructor;
	}
}
