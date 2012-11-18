// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pgsnapshot.v0_6.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.database.DbFeature;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * Provides functionality common to all entity feature daos.
 * 
 * @author Brett Henderson
 * @param <Tef>
 *            The entity feature type to be supported.
 * @param <Tdb>
 *            The entity feature database wrapper type to be used.
 */
public class EntityFeatureDao<Tef extends Storeable, Tdb extends DbFeature<Tef>> {
	private EntityFeatureMapper<Tdb> entityFeatureMapper;
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Provides access to the database.
	 * @param entityFeatureMapper
	 *            Provides entity type specific JDBC support.
	 */
	protected EntityFeatureDao(JdbcTemplate jdbcTemplate, EntityFeatureMapper<Tdb> entityFeatureMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		
		this.entityFeatureMapper = entityFeatureMapper;
	}
	
	
	/**
	 * Loads all instances of this feature for the specified entity from the database.
	 * 
	 * @param entityId
	 *            The unique identifier of the entity.
	 * @return All instances of this feature type for the entity.
	 */
	public Collection<Tdb> getAll(long entityId) {
		return jdbcTemplate.query(entityFeatureMapper.getSqlSelect("", true, true), entityFeatureMapper.getRowMapper());
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
		Map<String, Object> args;
		
		args = new HashMap<String, Object>();
		
		for (Tdb feature : features) {
			args.clear();
			entityFeatureMapper.populateParameters(args, feature);
			
			namedParameterJdbcTemplate.update(entityFeatureMapper.getSqlInsert(1), args);
		}
	}
	
	
	/**
	 * Removes the specified feature list from the database.
	 * 
	 * @param entityId
	 *            The id of the entity to remove.
	 */
	public void removeList(long entityId) {
		jdbcTemplate.update(entityFeatureMapper.getSqlDelete(true), entityId);
	}
}
