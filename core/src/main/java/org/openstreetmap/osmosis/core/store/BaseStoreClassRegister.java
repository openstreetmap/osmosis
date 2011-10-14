// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Provides common functionality to store class register implementations.
 */
public class BaseStoreClassRegister implements StoreClassRegister {
	private Map<Class<?>, Byte> classToByteMap;
	private Map<Byte, Class<?>> byteToClassMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public BaseStoreClassRegister() {
		classToByteMap = new HashMap<Class<?>, Byte>();
		byteToClassMap = new HashMap<Byte, Class<?>>();
	}
	
	
	/**
	 * Indicates if the class is recognized by the current register.
	 * 
	 * @param clazz
	 *            The class to be checked.
	 * @return True if the class is recognized, false otherwise.
	 */
	protected boolean isClassRecognized(Class<?> clazz) {
		return classToByteMap.containsKey(clazz);
	}
	
	
	/**
	 * Registers the class with the specified id.
	 * 
	 * @param clazz
	 *            The class to be registered.
	 * @param id
	 *            The unique identifier for the class.
	 */
	protected void registerClass(Class<?> clazz, byte id) {
		Byte objId;
		
		objId = Byte.valueOf(id);
		
		classToByteMap.put(clazz, objId);
		byteToClassMap.put(objId, clazz);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void storeIdentifierForClass(StoreWriter storeWriter, Class<?> clazz) {
		byte id;
		
		if (classToByteMap.containsKey(clazz)) {
			id = classToByteMap.get(clazz).byteValue();
		} else {
			throw new OsmosisRuntimeException("The class " + clazz + " is not supported by this store class register.");
		}
		
		storeWriter.writeByte(id);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Class<?> getClassFromIdentifier(StoreReader storeReader) {
		byte classId;
		Byte idObj;
		
		classId = storeReader.readByte();
		
		idObj = Byte.valueOf(classId);
		
		if (!byteToClassMap.containsKey(idObj)) {
			throw new OsmosisRuntimeException(
					"Byte " + classId + " is not a recognised class identifier, the data stream may be corrupt.");
		}
		
		return byteToClassMap.get(idObj);
	}
}
