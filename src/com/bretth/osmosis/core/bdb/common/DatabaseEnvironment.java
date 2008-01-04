// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.Completable;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;


/**
 * Manages a Berkeley DB database environment and its associated resources such as databases.
 *  
 * @author Brett Henderson
 */
public class DatabaseEnvironment implements Completable {
	
	private File directory;
	private Environment environment;
	private DatabaseConfig dbConfig;
	private Map<String, Database> dbMap;
	
	
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
	public DatabaseEnvironment(File directory, boolean create, boolean readOnly) {
		this.directory = directory;
		
		dbConfig = new DatabaseConfig();
		if (create) {
			dbConfig.setAllowCreate(create);
			// If creating a new environment, it must not already exist.
			dbConfig.setExclusiveCreate(true);
		}
		dbConfig.setReadOnly(readOnly);
		
		dbMap = new HashMap<String, Database>();
	}
	
	
	/**
	 * Returns a fully initialised environment instance. It will return an
	 * existing instance if one exists.
	 * 
	 * @return The environment instance.
	 */
	private Environment getEnvironment() {
		if (environment == null) {
			EnvironmentConfig envConfig;
			
			envConfig = new EnvironmentConfig();
			
			try {
				environment = new Environment(directory, envConfig);
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to create a new bdb environment.", e);
			}
		}
		
		return environment;
	}
	
	
	/**
	 * Opens a database and adds it to the internal database list for
	 * housekeeping. The client must not close the database.
	 * 
	 * @param name
	 *            The name of the database to open.
	 * @return The newly opened database.
	 */
	public Database openDatabase(String name) {
		try {
			Database database;
			
			database = environment.openDatabase(null, name, dbConfig);
			
			dbMap.put(name, database);
			
			return database;
			
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to create a new database.", e);
		}
	}
	
	
	/**
	 * Begins a new transaction on the environment. The transaction lifetime is
	 * owned by the caller and must be closed prior to calling complete on the
	 * environment.
	 * 
	 * @return The new transaction.
	 */
	public Transaction createTransaction() {
		try {
			TransactionConfig txnCfg;
			
			txnCfg = new TransactionConfig();
			txnCfg.setSync(true);
			
			return environment.beginTransaction(null, txnCfg);
			
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to create a new transaction.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		for (Database db : dbMap.values()) {
			try {
				db.close();
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to close a database.", e);
			}
		}
		
		try {
			getEnvironment().close();
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to close the bdb environment at location " + directory + ".", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (Database db : dbMap.values()) {
			try {
				db.close();
			} catch (DatabaseException e) {
				// Do nothing.
			}
		}
		dbMap.clear();
		
		if (environment != null) {
			try {
				environment.close();
			} catch (DatabaseException e) {
				// Do nothing.
			}
			environment = null;
		}
	}
}
