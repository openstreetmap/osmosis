// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.apidb.v0_6.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.MultipleSourceIterator;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Provides operations that act on on all entity types by combining operations from the underlying
 * DAO implementations.
 */
public class AllEntityDao implements ReplicationSource {
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public AllEntityDao(JdbcTemplate jdbcTemplate) {
		nodeDao = new NodeDao(jdbcTemplate);
		wayDao = new WayDao(jdbcTemplate);
		relationDao = new RelationDao(jdbcTemplate);
	}
	
	
	/**
	 * Retrieves the changes that have were made by a set of transactions.
	 * 
	 * @param predicates
	 *            Contains the predicates defining the transactions to be queried.
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory(ReplicationQueryPredicates predicates) {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			List<ReleasableIterator<ChangeContainer>> sources;
			MultipleSourceIterator<ChangeContainer> resultIterator;
			
			sources = new ArrayList<ReleasableIterator<ChangeContainer>>();
			sources.add(releasableContainer.add(nodeDao.getHistory(predicates)));
			sources.add(releasableContainer.add(wayDao.getHistory(predicates)));
			sources.add(releasableContainer.add(relationDao.getHistory(predicates)));
			
			resultIterator = new MultipleSourceIterator<ChangeContainer>(sources);
			
			releasableContainer.clear();
			
			return resultIterator;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * Retrieves the changes that have were made between two points in time.
	 * 
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory(Date intervalBegin, Date intervalEnd) {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			List<ReleasableIterator<ChangeContainer>> sources;
			MultipleSourceIterator<ChangeContainer> resultIterator;
			
			sources = new ArrayList<ReleasableIterator<ChangeContainer>>();
			sources.add(releasableContainer.add(nodeDao.getHistory(intervalBegin, intervalEnd)));
			sources.add(releasableContainer.add(wayDao.getHistory(intervalBegin, intervalEnd)));
			sources.add(releasableContainer.add(relationDao.getHistory(intervalBegin, intervalEnd)));
			
			resultIterator = new MultipleSourceIterator<ChangeContainer>(sources);
			
			releasableContainer.clear();
			
			return resultIterator;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * Retrieves all changes in the database.
	 * 
	 * @return An iterator pointing at the identified records.
	 */
	public ReleasableIterator<ChangeContainer> getHistory() {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			List<ReleasableIterator<ChangeContainer>> sources;
			MultipleSourceIterator<ChangeContainer> resultIterator;
			
			sources = new ArrayList<ReleasableIterator<ChangeContainer>>();
			sources.add(releasableContainer.add(nodeDao.getHistory()));
			sources.add(releasableContainer.add(wayDao.getHistory()));
			sources.add(releasableContainer.add(relationDao.getHistory()));
			
			resultIterator = new MultipleSourceIterator<ChangeContainer>(sources);
			
			releasableContainer.clear();
			
			return resultIterator;
			
		} finally {
			releasableContainer.release();
		}
	}
	
	
	/**
	 * Retrieves all current data in the database.
	 * 
	 * @return An iterator pointing at the current records.
	 */
	public ReleasableIterator<EntityContainer> getCurrent() {
		ReleasableContainer releasableContainer;
		
		releasableContainer = new ReleasableContainer();
		try {
			List<ReleasableIterator<EntityContainer>> sources;
			MultipleSourceIterator<EntityContainer> resultIterator;
			
			sources = new ArrayList<ReleasableIterator<EntityContainer>>();
			sources.add(releasableContainer.add(nodeDao.getCurrent()));
			sources.add(releasableContainer.add(wayDao.getCurrent()));
			sources.add(releasableContainer.add(relationDao.getCurrent()));
			
			resultIterator = new MultipleSourceIterator<EntityContainer>(sources);
			
			releasableContainer.clear();
			
			return resultIterator;
			
		} finally {
			releasableContainer.release();
		}
	}
}
