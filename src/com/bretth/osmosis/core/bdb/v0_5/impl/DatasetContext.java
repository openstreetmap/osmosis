// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

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
	private static final String DB_WAY = "way";
	private static final String DB_RELATION = "relation";
	private static final String DB_TILE_NODE = "tile_node";
	
	private DatabaseEnvironment env;
	private boolean initialized;
	private Database node;
	private Database way;
	private Database relation;
	private Database tileNode;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param home
	 *            The directory to store all data files in.
	 * @param create
	 *            If true a new environment will be created, otherwise it must
	 *            already exist.
	 * @param readOnly
	 *            If true, no updates will be allowed to the underlying data.
	 */
	public DatasetContext(File home, boolean create, boolean readOnly) {
		env = new DatabaseEnvironment(home, create, readOnly);
		
		initialized = false;
	}
	
	
	/**
	 * Ensures all databases are opened and ready for use.
	 */
	private void initialize() {
		if (node == null) {
			node = env.openDatabase(DB_NODE);
		}
		if (way == null) {
			way = env.openDatabase(DB_WAY);
		}
		if (relation == null) {
			relation = env.openDatabase(DB_RELATION);
		}
		if (tileNode == null) {
			tileNode = env.openDatabase(DB_TILE_NODE);
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
		
		return new TransactionContext(env.createTransaction(), node, way, relation, tileNode);
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
