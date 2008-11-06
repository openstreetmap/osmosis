package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.ReleasableStatementContainer;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.lifecycle.Releasable;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.IdentityColumnValueLoader;


/**
 * Creates and maintains changesets for a database conversation. This will
 * create a separate changeset for each user id making changes.
 * 
 * @author Brett Henderson
 */
public class ChangesetManager implements Releasable {
	private static final String SQL_INSERT_CHANGESET =
		"INSERT INTO changesets" +
		" (user_id, created_at, open, min_lat, max_lat, min_lon, max_lon)" +
		" VALUES" +
		" (?, NOW(), 0, -90, 90, -180, 180)";
	private DatabaseContext dbCtx;
	private Map<Integer, Long> userToChangesetMap;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement insertStatement;
	private IdentityColumnValueLoader identityLoader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public ChangesetManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		
		userToChangesetMap = new HashMap<Integer, Long>();
		statementContainer = new ReleasableStatementContainer();
		identityLoader = new IdentityColumnValueLoader(dbCtx);
	}
	
	
	private long insertChangeset(int userId) {
		if (insertStatement == null) {
			insertStatement = statementContainer.add(dbCtx.prepareStatementForStreaming(SQL_INSERT_CHANGESET));
		}
		
		try {
			int prmIndex;
			
			prmIndex = 1;
			insertStatement.setInt(prmIndex++, userId);
			insertStatement.executeUpdate();
			
			return identityLoader.getLastInsertId();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to insert a new changeset for user with id " + userId + ".", e);
		}
	}


	/**
	 * Creates and maintains a changeset for the specified user. If a changeset
	 * already exists for that user it will be re-used.
	 * 
	 * @param user
	 *            The user making changes.
	 * @return The database identifier of the changeset.
	 */
	public long obtainChangesetId(OsmUser user) {
		long changesetId;
		int userId;
		
		userId = user.getId();
		
		if (userToChangesetMap.containsKey(userId)) {
			changesetId = userToChangesetMap.get(userId);
		} else {
			changesetId = insertChangeset(userId);
			userToChangesetMap.put(userId, changesetId);
		}
		
		return changesetId;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		statementContainer.release();
	}
}
