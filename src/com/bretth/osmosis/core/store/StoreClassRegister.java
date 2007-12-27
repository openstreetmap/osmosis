package com.bretth.osmosis.core.store;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Allocates unique identifiers for class types written to a store and maintains
 * the relationship between classes and their identifiers.
 * 
 * @author Brett Henderson
 */
public class StoreClassRegister {
	private byte idSequence;
	private Map<Class<?>, Byte> classToByteMap;
	private Map<Byte, Constructor<?>> byteToConstructorMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public StoreClassRegister() {
		classToByteMap = new HashMap<Class<?>, Byte>();
		byteToConstructorMap = new HashMap<Byte, Constructor<?>>();
		idSequence = 0;
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
		try {
			return clazz.getConstructor(new Class [] {StoreReader.class, StoreClassRegister.class});
			
		} catch (NoSuchMethodException e) {
			throw new OsmosisRuntimeException("Class " + clazz.getName() + " does not have a constructor accepting a " + StoreReader.class.getName() + " argument, this is required for all Storeable classes.", e);
		}
	}
	
	
	/**
	 * Stores the unique identifier for the specified class to the store. If no
	 * identifier already exists exists, a new identifier is allocated and the
	 * class is registered internally.
	 * 
	 * @param storeWriter
	 *            The store to write class identification data to.
	 * @param clazz
	 *            The class for which an identifier is required.
	 */
	public void storeIdentifierForClass(StoreWriter storeWriter, Class<?> clazz) {
		byte id;
		
		if (classToByteMap.containsKey(clazz)) {
			id = classToByteMap.get(clazz).byteValue();
		} else {
			Byte objId;
			Constructor<?> constructor;
			
			if (idSequence >= Byte.MAX_VALUE) {
				throw new OsmosisRuntimeException(
					"This serialisation implementation only supports " +
					Byte.MAX_VALUE + " classes within a single stream."
				);
			}
			
			id = idSequence++;
			objId = Byte.valueOf(id);
			
			try {
				constructor = clazz.getConstructor(new Class [] {StoreReader.class, StoreClassRegister.class});
				
			} catch (NoSuchMethodException e) {
				throw new OsmosisRuntimeException("Class " + clazz.getName() + " does not have a constructor accepting a " + StoreReader.class.getName() + " argument, this is required for all Storeable classes.", e);
			}
			
			classToByteMap.put(clazz, objId);
			byteToConstructorMap.put(objId, constructor);
		}
		
		storeWriter.writeByte(id);
	}
	
	
	/**
	 * Returns the constructor of the class associated with the unique
	 * identifier in the store. An exception will be thrown if the identifier is
	 * not recognised.
	 * 
	 * @param storeReader
	 *            The store to read class identification information from.
	 * @return The constructor of the class associated with the identifier.
	 */
	public Constructor<?> getConstructorForClass(StoreReader storeReader) {
		byte classId;
		Byte idObj;
		
		classId = storeReader.readByte();
		
		idObj = Byte.valueOf(classId);
		
		if (!byteToConstructorMap.containsKey(idObj)) {
			throw new OsmosisRuntimeException("Byte " + classId + " is not a recognised class identifier, the data stream may be corrupt.");
		}
		
		return byteToConstructorMap.get(idObj);
	}
}
