// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Provides the ability to manipulate ways.
 * 
 * @author Brett Henderson
 */
public class WayBuilder extends EntityBuilder<Way> {
	private List<WayNode> wayNodes;
	
	
	/**
	 * Creates a new instance.
	 */
	public WayBuilder() {
		super();
		
		wayNodes = new ArrayList<WayNode>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The entity to initialise to.
	 */
	public WayBuilder(Way entity) {
		this();
		
		initialize(entity);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public WayBuilder(long id, int version, Date timestamp, OsmUser user) {
		this();
		
		initialize(id, version, timestamp, user);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public WayBuilder(long id, TimestampContainer timestampContainer, OsmUser user, int version) {
		this();
		
		initialize(id, version, timestampContainer, user);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public WayBuilder(StoreReader sr, StoreClassRegister scr) {
		this();
		
		initialize(new Way(sr, scr));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		buildEntity().store(sw, scr);
	}
	
	
	/**
	 * Initialises the state of this sub-class.
	 */
	private void initializeLocal() {
		wayNodes.clear();
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param way
	 *            The entity to initialise to.
	 * @return This object allowing method chaining.
	 */
	public WayBuilder initialize(Way way) {
		super.initialize(way);
		initializeLocal();
		wayNodes.addAll(way.getWayNodes());
		
		return this;
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param newId
	 *            The unique identifier.
	 * @param newVersion
	 *            The version of the entity.
	 * @param newTimestamp
	 *            The last updated timestamp.
	 * @param newUser
	 *            The user that last modified this entity.
	 * @return This object allowing method chaining.
	 */
	@Override
	public WayBuilder initialize(long newId, int newVersion, Date newTimestamp, OsmUser newUser) {
		super.initialize(newId, newVersion, newTimestamp, newUser);
		initializeLocal();
		
		return this;
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param newId
	 *            The unique identifier.
	 * @param newVersion
	 *            The version of the entity.
	 * @param newTimestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param newUser
	 *            The user that last modified this entity.
	 * @return This object allowing method chaining.
	 */
	@Override
	public WayBuilder initialize(long newId, int newVersion, TimestampContainer newTimestampContainer, OsmUser newUser) {
		super.initialize(newId, newVersion, newTimestampContainer, newUser);
		initializeLocal();
		
		return this;
	}
	
	
	/**
	 * Obtains the way nodes.
	 * 
	 * @return The way nodes.
	 */
	public List<WayNode> getWayNodes() {
		return wayNodes;
	}
	
	
	/**
	 * Remove all existing way nodes.
	 * 
	 * @return This object allowing method chaining.
	 */
	public WayBuilder clearWayNodes() {
		wayNodes.clear();
		
		return this;
	}
	
	
	/**
	 * Sets a new way nodes value.
	 * 
	 * @param newWayNodes
	 *            The new way nodes.
	 * @return This object allowing method chaining.
	 */
	public WayBuilder setWayNodes(List<WayNode> newWayNodes) {
		wayNodes.clear();
		wayNodes.addAll(newWayNodes);
		
		return this;
	}
	
	
	/**
	 * Adds a new way node.
	 * 
	 * @param wayNode
	 *            The new way node.
	 * @return This object allowing method chaining.
	 */
	public WayBuilder addWayNode(WayNode wayNode) {
		wayNodes.add(wayNode);
		
		return this;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way buildEntity() {
		return new Way(id, version, timestampContainer, user, tags, wayNodes);
	}
}
