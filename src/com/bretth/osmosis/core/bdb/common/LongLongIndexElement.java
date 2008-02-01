// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.util.LongAsInt;


/**
 * A single index key for a long-long index. This type of index key is typically
 * used for relating two entities with long identifiers.
 * 
 * @author Brett Henderson
 */
public class LongLongIndexElement implements Storeable {
	
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
	public LongLongIndexElement(long part1, long part2) {
		this.part1 = LongAsInt.longToInt(part1);
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
	public LongLongIndexElement(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger(), sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter writer, StoreClassRegister storeClassRegister) {
		writer.writeInteger(part1);
		writer.writeInteger(part2);
	}
	
	
	/**
	 * Returns part 1 of this index element.
	 * 
	 * @return The index id.
	 */
	public long getPart1() {
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
