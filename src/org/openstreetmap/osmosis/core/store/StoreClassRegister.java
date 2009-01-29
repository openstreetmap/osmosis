// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.store;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;


/**
 * Allocates unique identifiers for class types written to a store and maintains
 * the relationship between classes and their identifiers.
 * 
 * @author Brett Henderson
 */
public class StoreClassRegister {
	private byte idSequence;
	private Map<Class<?>, Byte> classToByteMap;
	private Map<Byte, Class<?>> byteToClassMap;
	
	
	/**
	 * Creates a new instance.
	 */
	public StoreClassRegister() {
		classToByteMap = new HashMap<Class<?>, Byte>();
		byteToClassMap = new HashMap<Byte, Class<?>>();
		idSequence = 0;
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
			
			if (idSequence >= Byte.MAX_VALUE) {
				throw new OsmosisRuntimeException(
					"This serialisation implementation only supports " +
					Byte.MAX_VALUE + " classes within a single stream."
				);
			}
			
			id = idSequence++;
			objId = Byte.valueOf(id);
			
			classToByteMap.put(clazz, objId);
			byteToClassMap.put(objId, clazz);
		}
		
		storeWriter.writeByte(id);
	}
	
	
	/**
	 * Returns the class associated with the unique identifier in the store. An
	 * exception will be thrown if the identifier is not recognised.
	 * 
	 * @param storeReader
	 *            The store to read class identification information from.
	 * @return The class associated with the identifier.
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
