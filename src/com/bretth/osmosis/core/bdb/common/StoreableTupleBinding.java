// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import com.bretth.osmosis.core.store.ObjectReader;
import com.bretth.osmosis.core.store.ObjectWriter;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.store.Storeable;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


/**
 * Provides a Berkeley DB tuple binding for all storeable class implementations.
 * 
 * @param <T>
 *            The object type to be stored.
 * @author Brett Henderson
 */
public class StoreableTupleBinding<T extends Storeable> extends TupleBinding {
	
	private TupleOutputStoreWriter storeWriter;
	private TupleInputStoreReader storeReader;
	private ObjectWriter objectWriter;
	private ObjectReader objectReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param storeableType
	 *            The class type to be supported.
	 */
	public StoreableTupleBinding(Class<?> storeableType) {
		SingleClassObjectSerializationFactory serializationFactory;
		
		serializationFactory = new SingleClassObjectSerializationFactory(storeableType);
		
		storeWriter = new TupleOutputStoreWriter();
		storeReader = new TupleInputStoreReader();
		
		objectWriter = serializationFactory.createObjectWriter(storeWriter, null);
		objectReader = serializationFactory.createObjectReader(storeReader, null);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T entryToObject(TupleInput input) {
		storeReader.input = input;
		
		return (T) objectReader.readObject();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void objectToEntry(Object object, TupleOutput output) {
		// Update the output destination in the store writer.
		storeWriter.output = output;
		
		objectWriter.writeObject((Storeable) object);
	}
}
