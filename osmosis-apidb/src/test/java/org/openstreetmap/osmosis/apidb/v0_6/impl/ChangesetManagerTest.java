// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import org.junit.Test;
import org.openstreetmap.osmosis.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.testutil.AbstractDataTest;


/**
 * Tests for the changeset manager class.
 * 
 * @author Brett Henderson
 */
public class ChangesetManagerTest extends AbstractDataTest {

    private final DatabaseUtilities dbUtils = new DatabaseUtilities(dataUtils);
    
    
    /**
     * Tests the changeset manager.
     */
    @Test
    public void testChangeset() {
    	DatabaseContext dbCtx;
    	UserManager userManager;
    	ChangesetManager changesetManager;
    	OsmUser user;
    	long changesetId;
    	
    	user = new OsmUser(1, "user");
    	changesetId = 2;
    	
    	dbCtx = dbUtils.createDatabaseContext();
    	
    	// Reset the database to a clean state.
    	dbUtils.truncateDatabase();
    	
    	userManager = new UserManager(dbCtx);
    	changesetManager = new ChangesetManager(dbCtx);
    	
    	userManager.addOrUpdateUser(user);
    	
    	// Create the changeset in the database.
    	changesetManager.addChangesetIfRequired(changesetId, user);
    	
    	// Make the same call which should just return if the changeset is already known.
    	changesetManager.addChangesetIfRequired(changesetId, user);
    	
    	// Create a new instance of the manager to verify that it copes with a non-cached changeset.
    	changesetManager = new ChangesetManager(dbCtx);
    	changesetManager.addChangesetIfRequired(changesetId, user);
    	
    	dbCtx.release();
    }
}
