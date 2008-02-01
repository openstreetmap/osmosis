// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
public class EnvironmentContext implements Completable {
	
	private static final Logger log = Logger.getLogger(EnvironmentContext.class.getName());
	
	
	private File home;
	private EnvironmentConfig envConfig;
	private Environment env;
	private DatabaseConfig dbConfig;
	private Map<String, Database> dbMap;
	
	
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
	public EnvironmentContext(File home, boolean create, boolean readOnly) {
		this.home = home;
		
		envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(create);
		envConfig.setTransactional(true);
		
		dbConfig = new DatabaseConfig();
		//dbConfig.setTransactional(true);
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
		if (env == null) {
			try {
				env = new Environment(home, envConfig);
			} catch (DatabaseException e) {
				throw new OsmosisRuntimeException("Unable to create a new bdb environment.", e);
			}
		}
		
		return env;
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
			
			database = getEnvironment().openDatabase(null, name, dbConfig);
			
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
			
			return getEnvironment().beginTransaction(null, txnCfg);
			
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
		dbMap.clear();
		
		try {
			log.fine("Cleaning database log.");
			getEnvironment().cleanLog();
			log.fine("Closing environment.");
			getEnvironment().close();
			log.fine("Environment closed successfully.");
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to close the bdb environment at location " + home + ".", e);
		}
		env = null;
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
		
		if (env != null) {
			try {
				env.close();
			} catch (DatabaseException e) {
				// Do nothing.
			}
			env = null;
		}
	}
}
