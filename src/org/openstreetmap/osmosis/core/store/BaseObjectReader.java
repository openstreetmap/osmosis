// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides functionality common to all object reader implementations.
 * 
 * @author Brett Henderson
 */
public abstract class BaseObjectReader implements ObjectReader {
	
	private StoreReader storeReader;
	private StoreClassRegister storeClassRegister;
	private StoreableConstructorCache constructorCache;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeReader
	 *            The store writer to read all serialised data from.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	public BaseObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister) {
		this.storeReader = storeReader;
		this.storeClassRegister = storeClassRegister;
		
		constructorCache = new StoreableConstructorCache();
	}
	
	
	/**
	 * Identifies the class using data from the underlying stream.
	 * 
	 * @param sr
	 *            The store reader to read all serialised data from.
	 * @param scr
	 *            The register for class to identifier mappings.
	 * @return The next class type in the data stream.
	 */
	protected abstract Class<?> readClassFromIdentifier(StoreReader sr, StoreClassRegister scr);
	
	
	/**
	 * Reads an object from storage using identifiers embedded in the stream to
	 * determine the correct class type to instantiate.
	 * 
	 * @return The re-instantiated object.
	 */
	public Storeable readObject() {
		Class<?> clazz;
		Constructor<?> constructor;
		
		clazz = readClassFromIdentifier(storeReader, storeClassRegister);
		
		constructor = constructorCache.getStoreableConstructor(clazz);
		
		try {
			return (Storeable) constructor.newInstance(new Object[] {storeReader, storeClassRegister});
			
		} catch (IllegalAccessException e) {
			throw new OsmosisRuntimeException(
					"The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof EndOfStoreException) {
				throw (EndOfStoreException) cause; 
			}
			throw new OsmosisRuntimeException(
					"The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		} catch (InstantiationException e) {
			throw new OsmosisRuntimeException(
					"The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		}
	}
}
