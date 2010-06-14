// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;


/**
 * Provides information about which features a database supports.
 * 
 * @author Brett Henderson
 */
public class DatabaseCapabilityChecker {
	private DatabaseContext dbCtx;
	private boolean initialized;
	private boolean isActionSupported;
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
			isActionSupported = dbCtx.doesTableExist("actions");
			isWayBboxSupported = dbCtx.doesColumnExist("ways", "bbox");
			isWayLinestringSupported = dbCtx.doesColumnExist("ways", "linestring");
			
			initialized = true;
		}
	}
	
	
	/**
	 * Indicates if action support is available.
	 * 
	 * @return True if supported, otherwise false.
	 */
	public boolean isActionSupported() {
		initialize();
		
		return isActionSupported;
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
