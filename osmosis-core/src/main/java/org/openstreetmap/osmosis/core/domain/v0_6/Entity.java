// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * A data class representing a single OSM entity. All top level data types
 * inherit from this class.
 * 
 * @author Brett Henderson
 */
public abstract class Entity implements Storeable {
	
	private CommonEntityData entityData;
	
	
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
	 * @deprecated As of 0.40, replaced by Entity(entityData).
	 */
	public Entity(long id, int version, Date timestamp, OsmUser user, long changesetId) {
		entityData = new CommonEntityData(id, version, timestamp, user, changesetId);
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
	 * @deprecated As of 0.40, replaced by Entity(entityData).
	 */
	public Entity(long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId) {
		entityData = new CommonEntityData(id, version, timestampContainer, user, changesetId);
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
	 * @param tags
	 *            The tags to apply to the object.
	 * @deprecated As of 0.40, replaced by Entity(entityData).
	 */
	public Entity(long id, int version, Date timestamp, OsmUser user, long changesetId, Collection<Tag> tags) {
		entityData = new CommonEntityData(id, version, timestamp, user, changesetId, tags);
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
	 * @param tags
	 *            The tags to apply to the object.
	 * @deprecated As of 0.40, replaced by Entity(entityData).
	 */
	public Entity(long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId,
			Collection<Tag> tags) {
		entityData = new CommonEntityData(id, version, timestampContainer, user, changesetId, tags);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The data to store in the entity. This instance is used directly and is not cloned.
	 */
	public Entity(CommonEntityData entityData) {
		this.entityData = entityData.getWriteableInstance();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param originalEntity
	 *            The entity to clone from.
	 */
	protected Entity(Entity originalEntity) {
		this.entityData = originalEntity.entityData.getWriteableInstance();
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
	public Entity(StoreReader sr, StoreClassRegister scr) {
		entityData = new CommonEntityData(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		entityData.store(sw, scr);
	}
	
	
	/**
	 * Compares the tags on this entity to the specified tags. The tag
	 * comparison is based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTags
	 *            The tags to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareTags(Collection<Tag> comparisonTags) {
		return entityData.compareTags(comparisonTags);
	}
	
	
	/**
	 * Returns the specific data type represented by this entity.
	 * 
	 * @return The entity type enum value.
	 */
	public abstract EntityType getType();


	/**
	 * Gets the identifier.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return entityData.getId();
	}


	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            The identifier.
	 */
	public void setId(long id) {
		entityData.setId(id);
	}
	
	
	/**
	 * Gets the version.
	 * 
	 * @return The version.
	 */
	public int getVersion() {
		return entityData.getVersion();
	}


	/**
	 * Sets the version.
	 * 
	 * @param version
	 *            The version.
	 */
	public void setVersion(int version) {
		entityData.setVersion(version);
	}
	
	
	/**
	 * Gets the timestamp in date form. This is the standard method for
	 * retrieving timestamp information.
	 * 
	 * @return The timestamp.
	 */
	public Date getTimestamp() {
		return entityData.getTimestamp();
	}


	/**
	 * Sets the timestamp in date form. This is the standard method of updating a timestamp.
	 * 
	 * @param timestamp
	 *            The timestamp.
	 */
	public void setTimestamp(Date timestamp) {
		entityData.setTimestamp(timestamp);
	}
	
	
	/**
	 * Gets the timestamp container object which may hold the timestamp in a
	 * different format. This is most useful if creating new copies of entities
	 * because it can avoid the need to parse timestamp information into Date
	 * form.
	 * 
	 * @return The timestamp container.
	 */
	public TimestampContainer getTimestampContainer() {
		return entityData.getTimestampContainer();
	}
	
	
	/**
	 * Sets the timestamp container object allowing the timestamp to be held in a different format.
	 * This should be used if a date is already held in a timestamp container, or if date parsing
	 * can be avoided.
	 * 
	 * @param timestampContainer
	 *            The timestamp container.
	 */
	public void setTimestampContainer(TimestampContainer timestampContainer) {
		entityData.setTimestampContainer(timestampContainer);
	}
	
	
	/**
	 * Gets the timestamp in a string format. If the entity already contains a
	 * string in string format it will return the original unparsed string
	 * instead of formatting a date object.
	 * 
	 * @param timestampFormat
	 *            The formatter to use for formatting the timestamp into a
	 *            string.
	 * @return The timestamp string.
	 */
	public String getFormattedTimestamp(TimestampFormat timestampFormat) {
		return entityData.getFormattedTimestamp(timestampFormat);
	}
	
	
	/**
	 * Returns the user who last edited the entity.
	 * 
	 * @return The user.
	 */
	public OsmUser getUser() {
		return entityData.getUser();
	}
	
	
	/**
	 * Sets the last modification user.
	 * 
	 * @param user
	 *            The user.
	 */
	public void setUser(OsmUser user) {
		entityData.setUser(user);
	}
	
	
	/**
	 * Gets the id of the changeset that this version of the entity was created by.
	 * 
	 * @return The changeset id.
	 */
	public long getChangesetId() {
		return entityData.getChangesetId();
	}
	
	
	/**
	 * Sets the id of the changeset that this version of the entity was created by.
	 * 
	 * @param changesetId
	 *            The changeset id.
	 */
	public void setChangesetId(long changesetId) {
		entityData.setChangesetId(changesetId);
	}


	/**
	 * Returns the attached tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The tags.
	 */
	public Collection<Tag> getTags() {
		return entityData.getTags();
	}


	/**
	 * Returns the attached meta tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The meta tags.
	 */
	public Map<String, Object> getMetaTags() {
		return entityData.getMetaTags();
	}


	/**
	 * Indicates if the object has been set to read-only. A read-only object
	 * must be cloned in order to make updates. This allows objects shared
	 * between multiple threads to be locked for thread safety.
	 * 
	 * @return True if the object is read-only.
	 */
	public boolean isReadOnly() {
		return entityData.isReadOnly();
	}


	/**
	 * Ensures that the object is writeable. If not an exception will be thrown.
	 * This is intended to be called within all update methods.
	 */
	protected void assertWriteable() {
		entityData.assertWriteable();
	}


	/**
	 * Configures the object to be read-only. This should be called if the object is to be processed
	 * by multiple threads concurrently. It updates the read-only status of the object, and makes
	 * all collections unmodifiable. This must be overridden by sub-classes to make their own
	 * collections unmodifiable.
	 */
	public void makeReadOnly() {
		entityData.makeReadOnly();
	}


	/**
	 * Returns a writeable instance of this entity. If the object is read-only a clone is created,
	 * if it is already writeable then this object is returned.
	 * 
	 * @return A writeable instance of this entity.
	 */
	public abstract Entity getWriteableInstance();
}
