// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.common.NoSuchRecordException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Performs all user-specific db operations.
 * 
 * @author Brett Henderson
 */
public class UserDao {
	private static final String SELECT_USER = "SELECT id, name FROM users WHERE id = ?";
	private static final String INSERT_USER = "INSERT INTO users(id, name) VALUES(?, ?)";
	private static final String UPDATE_USER = "UPDATE users SET name = ? WHERE id = ?";
	
	private JdbcTemplate jdbcTemplate;
	private ActionDao actionDao;
	private UserRowMapper rowMapper;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param actionDao
	 *            The dao to use for adding action records to the database.
	 */
	public UserDao(DatabaseContext dbCtx, ActionDao actionDao) {
		this.actionDao = actionDao;
		
		jdbcTemplate = dbCtx.getJdbcTemplate();
		
		rowMapper = new UserRowMapper();
	}
	
	
	/**
	 * Loads the specified way from the database.
	 * 
	 * @param userId
	 *            The unique identifier of the user.
	 * @return The loaded user.
	 */
	public OsmUser getUser(long userId) {
		OsmUser user;
		
		try {
			user = jdbcTemplate.queryForObject(SELECT_USER, rowMapper, userId);
		} catch (EmptyResultDataAccessException e) {
			throw new NoSuchRecordException("User " + userId + " doesn't exist.", e);
		}
		
		return user;
	}
	
	
	/**
	 * Adds the specified user to the database.
	 * 
	 * @param user
	 *            The user to add.
	 */
	public void addUser(OsmUser user) {
		jdbcTemplate.update(INSERT_USER, user.getId(), user.getName());
		
		actionDao.addAction(ActionDataType.USER, ChangesetAction.CREATE, user.getId());
	}
	
	
	/**
	 * Updates the specified user record in the database.
	 * 
	 * @param user
	 *            The user to update.
	 */
	public void updateUser(OsmUser user) {
		jdbcTemplate.update(UPDATE_USER, user.getName(), user.getId());
		
		actionDao.addAction(ActionDataType.USER, ChangesetAction.MODIFY, user.getId());
	}
}
