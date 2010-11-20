// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsimple.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.pgsimple.common.BaseDao;
import org.openstreetmap.osmosis.pgsimple.common.DatabaseContext;


/**
 * Provides functionality common to all entity feature daos.
 * 
 * @author Brett Henderson
 * @param <Tef>
 *            The entity feature type to be supported.
 * @param <Tdb>
 *            The entity feature database wrapper type to be used.
 */
public class EntityFeatureDao<Tef extends Storeable, Tdb extends DbFeature<Tef>> extends BaseDao {
	private static final Logger LOG = Logger.getLogger(EntityFeatureDao.class.getName());
	/**
	 * Provides jdbc mapping functionality for this entity feature type.
	 */
	protected EntityFeatureMapper<Tdb> entityFeatureBuilder;
	private PreparedStatement getStatement;
	private PreparedStatement insertStatement;
	private PreparedStatement deleteStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param entityFeatureBuilder
	 *            Provides entity type specific JDBC support.
	 */
	protected EntityFeatureDao(DatabaseContext dbCtx, EntityFeatureMapper<Tdb> entityFeatureBuilder) {
		super(dbCtx);
		
		this.entityFeatureBuilder = entityFeatureBuilder;
	}
	
	
	/**
	 * Loads all instances of this feature for the specified entity from the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return All instances of this feature type for the entity.
	 */
	public Collection<Tdb> getAll(long entityId) {
		ResultSet resultSet = null;
		
		if (getStatement == null) {
			getStatement = prepareStatement(entityFeatureBuilder.getSqlSelect(true, true));
		}
		
		try {
			List<Tdb> resultList;
			
			getStatement.setLong(1, entityId);
			resultSet = getStatement.executeQuery();
			
			resultList = new ArrayList<Tdb>();
			while (resultSet.next()) {
				resultList.add(entityFeatureBuilder.buildEntity(resultSet));
			}
			
			resultSet.close();
			resultSet = null;
			
			return resultList;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Query failed for "
					+ entityFeatureBuilder.getEntityName() + " " + entityId + ".", e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// We are already in an error condition so log and continue.
					LOG.log(Level.WARNING, "Unable to close result set.", e);
				}
			}
		}
	}
	
	
	/**
	 * Loads all instances of this feature for the specified entity from the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return All instances of this feature type for the entity.
	 */
	public Collection<Tef> getAllRaw(long entityId) {
		Collection<Tdb> dbFeatures;
		Collection<Tef> rawFeatures;
		
		dbFeatures = getAll(entityId);
		rawFeatures = new ArrayList<Tef>(dbFeatures.size());
		for (Tdb dbFeature : dbFeatures) {
			rawFeatures.add(dbFeature.getFeature());
		}
		
		return rawFeatures;
	}
	
	
	/**
	 * Adds the specified features to the database.
	 * 
	 * @param features
	 *            The features to add.
	 */
	public void addAll(Collection<Tdb> features) {
		if (insertStatement == null) {
			insertStatement = prepareStatement(entityFeatureBuilder.getSqlInsert(1));
		}
		
		for (Tdb feature : features) {
			try {
				entityFeatureBuilder.populateEntityParameters(insertStatement, 1, feature);
				insertStatement.executeUpdate();
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
						"Insert failed for "
						+ entityFeatureBuilder.getEntityName() + " " + feature.getEntityId()
						+ "."
				);
			}
		}
	}
	
	
	/**
	 * Removes the specified feature list from the database.
	 * 
	 * @param entityId
	 *            The id of the entity to remove.
	 */
	public void removeList(long entityId) {
		int prmIndex;
		
		if (deleteStatement == null) {
			deleteStatement = prepareStatement(entityFeatureBuilder.getSqlDelete(true));
		}
		
		try {
			prmIndex = 1;
			deleteStatement.setLong(prmIndex++, entityId);
			deleteStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Delete failed for "
					+ entityFeatureBuilder.getEntityName() + " "
					+ entityId + "."
			);
		}
	}
	
	
	/**
	 * Returns an iterator providing access to all entity features of this type
	 * in the database.
	 * 
	 * @return The entity feature iterator.
	 */
	public ReleasableIterator<Tdb> iterate() {
		return new EntityFeatureTableReader<Tef, Tdb>(getDatabaseContext(), entityFeatureBuilder);
	}
}
