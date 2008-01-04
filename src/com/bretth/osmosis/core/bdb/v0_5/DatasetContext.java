// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5;

import java.io.File;

import com.bretth.osmosis.core.bdb.common.DatabaseEnvironment;
import com.bretth.osmosis.core.store.Completable;
import com.sleepycat.je.Database;


/**
 * Manages a Berkeley DB database environment for the "dataset" specific schema.
 * 
 * @author Brett Henderson
 */
public class DatasetContext implements Completable {
	private static final String DB_NODE = "node";
	
	private DatabaseEnvironment env;
	private boolean initialized;
	private Database node;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param directory
	 *            The directory to store all data files in.
	 * @param create
	 *            If true a new environment will be created, otherwise it must
	 *            already exist.
	 * @param readOnly
	 *            If true, no updates will be allowed to the underlying data.
	 */
	public DatasetContext(File directory, boolean create, boolean readOnly) {
		env = new DatabaseEnvironment(directory, create, readOnly);
		
		initialized = false;
	}
	
	
	/**
	 * Ensures all databases are opened and ready for use.
	 */
	private void initialize() {
		if (node == null) {
			node = env.openDatabase(DB_NODE);
		}
		
		initialized = true;
	}
	
	
	/**
	 * Creates a new transaction context for interacting with the schema.
	 * 
	 * @return The new transaction context.
	 */
	public TransactionContext createTransaction() {
		if (!initialized) {
			initialize();
		}
		
		return new TransactionContext(env.createTransaction(), node);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		env.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		env.release();
	}
}
