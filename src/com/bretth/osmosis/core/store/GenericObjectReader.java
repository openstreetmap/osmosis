package com.bretth.osmosis.core.store;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides functionality to deserialise a Storeable implementation from a store.
 * 
 * @author Brett Henderson
 */
public class GenericObjectReader implements ObjectReader {
	private StoreReader storeReader;
	private StoreClassRegister storeClassRegister;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeReader
	 *            The store writer to write all serialised data to.
	 * @param storeClassRegister
	 *            The register for class to identifier mappings.
	 */
	public GenericObjectReader(StoreReader storeReader, StoreClassRegister storeClassRegister) {
		this.storeReader = storeReader;
		this.storeClassRegister = storeClassRegister;
	}
	
	
	/**
	 * Reads an object from storage using identifiers embedded in the stream to
	 * determine the correct class type to instantiate.
	 * 
	 * @return The re-instantiated object.
	 */
	@SuppressWarnings("unchecked")
	public Storeable readObject() {
		Constructor<?> constructor;
		
		try {
			// Obtain the class constructor based on the class identifier.
			constructor = storeClassRegister.getConstructorForClass(storeReader);
		} catch (EndOfStoreException e) {
			throw new NoMoreObjectsInStoreException("No more objects are available in the store.", e.getCause());
		}
		
		try {
			return (Storeable) constructor.newInstance(new Object[] {storeReader, storeClassRegister});
			
		} catch (IllegalAccessException e) {
			throw new OsmosisRuntimeException("The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		} catch (InvocationTargetException e) {
			throw new OsmosisRuntimeException("The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		} catch (InstantiationException e) {
			throw new OsmosisRuntimeException("The class " + constructor.getDeclaringClass().getName() + " could not be instantiated.", e);
		}
	}

}
