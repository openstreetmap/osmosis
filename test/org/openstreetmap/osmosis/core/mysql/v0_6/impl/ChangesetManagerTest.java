// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6.impl;

import org.junit.Assert;
import org.junit.Test;

import org.openstreetmap.osmosis.core.database.AuthenticationPropertiesLoader;
import org.openstreetmap.osmosis.core.database.DatabaseConstants;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;

import data.util.DataFileUtilities;

/**
 * Tests for the changeset manager class.
 * 
 * @author Brett Henderson
 */
public class ChangesetManagerTest {
	
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	private DatabaseContext createDatabaseContext() {
		AuthenticationPropertiesLoader credentialsLoader;
		DatabaseLoginCredentials credentials;
		
		credentials = new DatabaseLoginCredentials(
			DatabaseConstants.TASK_DEFAULT_HOST,
			DatabaseConstants.TASK_DEFAULT_DATABASE,
			DatabaseConstants.TASK_DEFAULT_USER,
			DatabaseConstants.TASK_DEFAULT_PASSWORD,
			DatabaseConstants.TASK_DEFAULT_FORCE_UTF8,
			DatabaseConstants.TASK_DEFAULT_PROFILE_SQL
		);
		credentialsLoader = new AuthenticationPropertiesLoader(fileUtils.getDataFile("v0_6/mysql-authfile.txt"));
		credentialsLoader.updateLoginCredentials(credentials);
		return new DatabaseContext(credentials);
	}
	
	
	/**
	 * Tests that the changeset manager allocates a single changeset for
	 * multiple entities owned by a single user, a different changeset per user,
	 * and creates a new changeset for a user when the maximum number of
	 * entities is reached.
	 */
	@Test
	public void testChangesetAllocation() {
		DatabaseContext dbCtx;
		ChangesetManager manager;
		OsmUser user1;
		OsmUser user2;
		long changesetIdUser1Current;
		long changesetIdUser2Current;
		long changesetIdUser1Previous;
		long changesetIdUser2Previous;
		
		dbCtx = createDatabaseContext();
		manager = new ChangesetManager(dbCtx);
		
		user1 = new OsmUser(1, "user1");
		user2 = new OsmUser(2, "user2");
		
		changesetIdUser1Previous = -1;
		changesetIdUser2Previous = -1;
		for (int i = 0; i < 50000; i++) {
			changesetIdUser1Current = manager.obtainChangesetId(user1);
			changesetIdUser2Current = manager.obtainChangesetId(user2);
			
			if (i > 1) {
				Assert.assertEquals(
						"User 1 changeset id should not be re-used.",
						changesetIdUser1Previous, changesetIdUser1Current);
				Assert.assertEquals(
						"User 2 changeset id should not be re-used.",
						changesetIdUser2Previous, changesetIdUser2Current);
			}
			
			changesetIdUser1Previous = changesetIdUser1Current;
			changesetIdUser2Previous = changesetIdUser2Current;
		}
		
		// The next changeset ids should change because we've reached the maximum.
		changesetIdUser1Current = manager.obtainChangesetId(user1);
		changesetIdUser2Current = manager.obtainChangesetId(user2);
		Assert.assertFalse(
				"User 1 changeset id should change after the maximum number of entities has been reached.",
				changesetIdUser1Previous == changesetIdUser1Current);
		Assert.assertFalse(
				"User 2 changeset id should change after the maximum number of entities has been reached.",
				changesetIdUser2Previous == changesetIdUser2Current);
		
		manager.release();
		dbCtx.release();
	}
}
