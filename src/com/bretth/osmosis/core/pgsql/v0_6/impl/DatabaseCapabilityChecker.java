package com.bretth.osmosis.core.pgsql.v0_6.impl;

import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Provides information about which features a database supports.
 * 
 * @author Brett Henderson
 */
public class DatabaseCapabilityChecker {
	private DatabaseContext dbCtx;
	private boolean initialized;
	private boolean isWayBboxSupported;
	private boolean isWayLinestringSupported;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx The database context to use for accessing the database.
	 */
	public DatabaseCapabilityChecker(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		initialized = false;
	}
	
	
	private void initialize() {
		if (!initialized) {
			isWayBboxSupported = dbCtx.doesColumnExist("ways", "bbox");
			isWayLinestringSupported = dbCtx.doesColumnExist("ways", "linestring");
			
			initialized = true;
		}
	}
	
	
	/**
	 * Indicates if way bounding box support is available.
	 * 
	 * @return True if supported, otherwise false.
	 */
	public boolean isWayBboxSupported() {
		initialize();
		
		return isWayBboxSupported;
	}
	
	
	/**
	 * Indicates if way linestring support is available.
	 * 
	 * @return True if supported, otherwise false.
	 */
	public boolean isWayLinestringSupported() {
		initialize();
		
		return isWayLinestringSupported;
	}
}
