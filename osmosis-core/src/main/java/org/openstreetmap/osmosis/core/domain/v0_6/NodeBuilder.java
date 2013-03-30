// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;


/**
 * Provides the ability to manipulate nodes.
 * 
 * @author Brett Henderson
 * 
 * @deprecated Builder classes are not required because entities are now writeable.
 */
@Deprecated
public class NodeBuilder extends EntityBuilder<Node> {
	private double latitude;
	private double longitude;
	
	
	/**
	 * Creates a new instance.
	 */
	public NodeBuilder() {
		super();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The entity to initialise to.
	 */
	public NodeBuilder(Node entity) {
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
	 * @param changesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @param latitude
	 *            The latitude of the node.
	 * @param longitude
	 *            The longitude of the entity.
	 */
	public NodeBuilder(long id, int version, Date timestamp, OsmUser user, long changesetId, double latitude,
			double longitude) {
		this();
		
		initialize(id, version, timestamp, user, changesetId, latitude, longitude);
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
	 * @param changesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @param latitude
	 *            The latitude of the node.
	 * @param longitude
	 *            The longitude of the entity.
	 */
	public NodeBuilder(
			long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId,
			double latitude, double longitude) {
		this();
		
		initialize(id, version, timestampContainer, user, changesetId, latitude, longitude);
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
	public NodeBuilder(StoreReader sr, StoreClassRegister scr) {
		this();
		
		initialize(new Node(sr, scr));
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
	 * 
	 * @param newLatitude The latitude of the node.
	 * @param newLongitude The longitude of the node.
	 */
	private void initializeLocal(double newLatitude, double newLongitude) {
		this.latitude = newLatitude;
		this.longitude = newLongitude;
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param node
	 *            The entity to initialise to.
	 * @return This object allowing method chaining.
	 */
	public NodeBuilder initialize(Node node) {
		super.initialize(node);
		initializeLocal(node.getLatitude(), node.getLongitude());
		
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
	 * @param newChangesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @param newLatitude
	 *            The latitude of the node.
	 * @param newLongitude
	 *            The longitude of the node.
	 * @return This object allowing method chaining.
	 */
	public NodeBuilder initialize(
			long newId, int newVersion, Date newTimestamp, OsmUser newUser, long newChangesetId,
			double newLatitude, double newLongitude) {
		super.initialize(newId, newVersion, newTimestamp, newUser, newChangesetId);
		initializeLocal(newLatitude, newLongitude);
		
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
	 * @param newChangesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @param newLatitude
	 *            The latitude of the node.
	 * @param newLongitude
	 *            The longitude of the node.
	 * @return This object allowing method chaining.
	 */
	public NodeBuilder initialize(
			long newId, int newVersion, TimestampContainer newTimestampContainer,
			OsmUser newUser, long newChangesetId, double newLatitude, double newLongitude) {
		super.initialize(newId, newVersion, newTimestampContainer, newUser, newChangesetId);
		initializeLocal(newLatitude, newLongitude);
		
		return this;
	}
	
	
	/**
	 * Sets a new latitude value.
	 * 
	 * @param newLatitude
	 *            The new latitude.
	 * @return This object allowing method chaining.
	 */
	public NodeBuilder setLatitude(double newLatitude) {
		this.latitude = newLatitude;
		
		return this;
	}
	
	
	/**
	 * Gets the current latitude value.
	 * 
	 * @return The latitude.
	 */
	public double getLatitude() {
		return latitude;
	}
	
	
	/**
	 * Sets a new longitude value.
	 * 
	 * @param newLongitude
	 *            The new longitude.
	 * @return This object allowing method chaining.
	 */
	public NodeBuilder setLongitude(double newLongitude) {
		this.longitude = newLongitude;
		
		return this;
	}
	
	
	/**
	 * Gets the current longitude value.
	 * 
	 * @return The longitude.
	 */
	public double getLongitude() {
		return longitude;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node buildEntity() {
		return new Node(id, version, timestampContainer, user, changesetId, tags, latitude, longitude);
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        String name = null;
        for (Tag tag : tags) {
            if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("name")) {
                name = tag.getValue();
                break;
            }
        }
        if (name != null) {
            return "NodeBuilder(id=" + getId() + ", #tags=" +  getTags().size() + ", name='" + name + "')";
        }
        return "NodeBuilder(id=" + getId() + ", #tags=" +  getTags().size() + ")";
    }
}
