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
	 * Returns the unique identifier for the specified class. If none exists, a
	 * new identifier is allocated and the class is registered internally.
	 * 
	 * @param clazz
	 *            The class for which an identifier is required.
	 * @return The unique identifier representing this class.
	 */
	public byte getIdentifierForClass(Class<?> clazz) {
		if (classToByteMap.containsKey(clazz)) {
			return classToByteMap.get(clazz).byteValue();
		} else {
			byte id;
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
			
			return id;
		}
	}
	
	
	/**
	 * Returns the class associated with the unique identifier. An exception
	 * will be thrown if the identifier is not recognised.
	 * 
	 * @param classId
	 *            The unique identifier for which the associated class is
	 *            required.
	 * @return The class associated with the identifier.
	 */
	public Constructor<?> getConstructorForClassId(byte classId) {
		Byte idObj;
		
		idObj = Byte.valueOf(classId);
		
		if (!byteToConstructorMap.containsKey(idObj)) {
			throw new OsmosisRuntimeException("Byte " + classId + " is not a recognised class identifier, the data stream may be corrupt.");
		}
		
		return byteToConstructorMap.get(idObj);
	}
}
