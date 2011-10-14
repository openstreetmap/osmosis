// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;


/**
 * A single index element for an int-long index.
 * 
 * @author Brett Henderson
 */
public class IntegerLongIndexElement implements IndexElement<Integer> {
	
	/**
	 * The value identifier.
	 */
	private int id;
	
	/**
	 * The data value.
	 */
	private long value;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The value identifier.
	 * @param value
	 *            The data value.
	 */
	public IntegerLongIndexElement(int id, long value) {
		this.id = id;
		this.value = value;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public IntegerLongIndexElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger(), sr.readLong());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeInteger(id);
		writer.writeLong(value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getKey() {
		// This will cause the sign of the identifier to be ignored resulting in
		// unsigned ordering of index values.
		return id;
	}
	
	
	/**
	 * Returns the id of this index element.
	 * 
	 * @return The index id.
	 */
	public int getId() {
		return id;
	}
	
	
	/**
	 * Returns the value of this index element.
	 * 
	 * @return The index value.
	 */
	public long getValue() {
		return value;
	}
}
