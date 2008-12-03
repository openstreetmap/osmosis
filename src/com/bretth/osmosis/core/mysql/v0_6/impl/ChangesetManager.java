package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.OsmosisConstants;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.ReleasableStatementContainer;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.lifecycle.Releasable;
import com.bretth.osmosis.core.lifecycle.ReleasableContainer;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.mysql.common.IdentityColumnValueLoader;
import com.bretth.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * Creates and maintains changesets for a database conversation. This will
 * create a separate changeset for each user id making changes.
 * 
 * @author Brett Henderson
 */
public class ChangesetManager implements Releasable {
	private static final String SQL_INSERT_CHANGESET =
		"INSERT INTO changesets" +
		" (user_id, created_at, min_lat, max_lat, min_lon, max_lon, closed_at, num_changes)" +
		" VALUES" +
		" (?, NOW(), " +
		FixedPrecisionCoordinateConvertor.convertToFixed(-90) + ", " +
		FixedPrecisionCoordinateConvertor.convertToFixed(90) + ", " +
		FixedPrecisionCoordinateConvertor.convertToFixed(-180) + ", " +
		FixedPrecisionCoordinateConvertor.convertToFixed(180) +
		", NOW(), 0)";
	
	private static final String SQL_INSERT_CHANGESET_TAG =
		"INSERT INTO changeset_tags (id, k, v)" +
		" VALUES (?, 'created_by', 'Osmosis " +
		OsmosisConstants.VERSION +
		"'), (?, 'replication', 'true')";
	
	private DatabaseContext dbCtx;
	private Map<Integer, Long> userToChangesetMap;
	private ReleasableContainer releasableContainer;
	private ReleasableStatementContainer statementContainer;
	private PreparedStatement insertStatement;
	private PreparedStatement insertTagStatement;
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
		releasableContainer = new ReleasableContainer();
		statementContainer = new ReleasableStatementContainer();
		identityLoader = new IdentityColumnValueLoader(dbCtx);
		
		releasableContainer.add(statementContainer);
		releasableContainer.add(identityLoader);
	}
	
	
	private long insertChangeset(int userId) {
		if (insertStatement == null) {
			insertStatement = statementContainer.add(dbCtx.prepareStatement(SQL_INSERT_CHANGESET));
			insertTagStatement = statementContainer.add(dbCtx.prepareStatement(SQL_INSERT_CHANGESET_TAG));
		}
		
		try {
			long changesetId;
			int prmIndex;
			
			// Insert the new changeset record.
			prmIndex = 1;
			insertStatement.setInt(prmIndex++, userId);
			insertStatement.executeUpdate();
			
			changesetId = identityLoader.getLastInsertId();
			
			// Insert the changeset tags.
			prmIndex = 1;
			insertTagStatement.setLong(prmIndex++, changesetId);
			insertTagStatement.setLong(prmIndex++, changesetId);
			insertTagStatement.executeUpdate();
			
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
		releasableContainer.release();
	}
}
