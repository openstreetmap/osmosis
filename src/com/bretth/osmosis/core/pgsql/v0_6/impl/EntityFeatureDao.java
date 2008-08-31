// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.BaseDao;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityFeature;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.Storeable;


/**
 * Provides functionality common to all entity feature daos.
 * 
 * @author Brett Henderson
 * @param <Tef>
 *            The entity feature type to be supported.
 * @param <Tdb>
 *            The entity feature database wrapper type to be used.
 */
public class EntityFeatureDao<Tef extends Storeable, Tdb extends DBEntityFeature<Tef>> extends BaseDao {
	
	/**
	 * Provides jdbc mapping functionality for this entity feature type.
	 */
	protected EntityFeatureBuilder<Tdb> entityFeatureBuilder;
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
	protected EntityFeatureDao(DatabaseContext dbCtx, EntityFeatureBuilder<Tdb> entityFeatureBuilder) {
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
	public List<Tdb> getList(long entityId) {
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
					+ entityFeatureBuilder.getEntityName() + " " + entityId + ".");
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
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
	public List<Tef> getRawList(long entityId) {
		List<Tdb> dbList;
		List<Tef> rawList;
		
		dbList = getList(entityId);
		rawList = new ArrayList<Tef>(dbList.size());
		for (Tdb dbFeature : dbList) {
			rawList.add(dbFeature.getEntityFeature());
		}
		
		return rawList;
	}
	
	
	/**
	 * Adds the specified feature list to the database.
	 * 
	 * @param featureList
	 *            The list of features to add.
	 */
	public void addList(List<Tdb> featureList) {
		if (insertStatement == null) {
			insertStatement = prepareStatement(entityFeatureBuilder.getSqlInsert());
		}
		
		for (Tdb feature : featureList) {
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
				"Delete failed for " +
				entityFeatureBuilder.getEntityName() + " "
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
