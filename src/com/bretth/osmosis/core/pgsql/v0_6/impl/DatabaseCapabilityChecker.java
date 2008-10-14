package com.bretth.osmosis.core.pgsql.v0_6.impl;

import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Provides information about which features a database supports.
 * 
 * @author Brett Henderson
 */
public class DatabaseCapabilityChecker {
	private DatabaseContext dbCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx The database context to use for accessing the database.
	 */
	public DatabaseCapabilityChecker(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
	}
	
	
	/**
	 * Indicates if way bounding box support is available.
	 * 
	 * @return True if supported, otherwise false.
	 */
	public boolean isWayBboxSupported() {
		return dbCtx.doesColumnExist("ways", "bbox");
	}
	
	
	/**
	 * Indicates if way linestring support is available.
	 * 
	 * @return True if supported, otherwise false.
	 */
	public boolean isWayLinestringSupported() {
		return dbCtx.doesColumnExist("ways", "linestring");
	}
}
