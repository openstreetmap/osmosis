// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.osmosis.core.container.v0_6.EntityManager;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.pgsnapshot.common.NoSuchRecordException;


/**
 * Provides postgres entity manager support allowing entities to be manipulated via a common dataset
 * interface.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The entity type to be supported.
 */
public class PostgreSqlEntityManager<T extends Entity> implements EntityManager<T> {
	
	private EntityDao<T> entityDao;
	private UserDao userDao;
	private Set<Integer> userSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityDao
	 *            The dao allowing manipulation of a specific entity type.
	 * @param userDao
	 *            The user dao allowing user entries to be updated or created.
	 */
	public PostgreSqlEntityManager(EntityDao<T> entityDao, UserDao userDao) {
		this.entityDao = entityDao;
		this.userDao = userDao;
		
		userSet = new HashSet<Integer>();
	}
	
	
	/**
	 * Writes the specified user to the database.
	 * 
	 * @param user
	 *            The user to write.
	 */
	private void writeUser(OsmUser user) {
		// Entities without a user assigned should not be written.
		if (!OsmUser.NONE.equals(user)) {
			// Users will only be updated in the database once per changeset
			// run.
			if (!userSet.contains(user.getId())) {
				int userId;
				OsmUser existingUser;

				userId = user.getId();

				try {
					existingUser = userDao.getUser(userId);

					if (!user.equals(existingUser)) {
						userDao.updateUser(user);
					}

				} catch (NoSuchRecordException e) {
					userDao.addUser(user);
				}

				userSet.add(user.getId());
			}
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(T entity) {
		writeUser(entity.getUser());
		entityDao.addEntity(entity);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists(long id) {
		return entityDao.exists(id);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getEntity(long id) {
		return entityDao.getEntity(id);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<T> iterate() {
		return entityDao.iterate();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(T entity) {
		writeUser(entity.getUser());
		entityDao.modifyEntity(entity);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntity(long entityId) {
		entityDao.removeEntity(entityId);
	}
}
