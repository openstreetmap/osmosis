// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.util.LongAsInt;


/**
 * A single index key for an uint-long index. This type of index key is
 * typically used for grouping sets of related objects with long identifiers by
 * some integer grouping such as a tile. "unsigned" refers to the sorting used
 * within the key where the integer is treated as a 32-bit unsigned integer.
 * 
 * @author Brett Henderson
 */
public class UnsignedIntegerLongIndexElement implements Storeable {
	
	/**
	 * Part 1 of the key.
	 */
	private int part1;
	
	/**
	 * Part 2 of the key.
	 */
	private int part2;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param part1
	 *            Part 1 of the key.
	 * @param part2
	 *            Part 2 of the key.
	 */
	public UnsignedIntegerLongIndexElement(int part1, long part2) {
		this.part1 = part1;
		this.part2 = LongAsInt.longToInt(part2);
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
	public UnsignedIntegerLongIndexElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger() ^ 0x80000000, sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		// Toggle the upper bit of the integer so that the underlying bdb
		// implementation sorts it as unsigned.
		writer.writeInteger(part1 ^ 0x80000000);
		// Write the long (currently represented as an int) as a standard signed value.
		writer.writeInteger(part2);
	}
	
	
	/**
	 * Returns part 1 of this index element.
	 * 
	 * @return The index id.
	 */
	public int getPart1() {
		return part1;
	}
	
	
	/**
	 * Returns part 2 of this index element.
	 * 
	 * @return The index value.
	 */
	public long getPart2() {
		return part2;
	}
}
