// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.store.Storeable;
import org.openstreetmap.osmosis.core.util.LazyHashMap;
import org.openstreetmap.osmosis.core.util.LongAsInt;


/**
 * Contains data common to all entity types. This is separated from the entity class to allow it to
 * be instantiated before all the data required for a full entity is available.
 */
public class CommonEntityData implements Storeable {
	
	private long id;
	private int version;
	private int changesetId;
	private TimestampContainer timestampContainer;
	private OsmUser user;
	private TagCollection tags;
	private Map<String, Object> metaTags;
	private boolean readOnly;
	
	
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
	 */
	public CommonEntityData(long id, int version, Date timestamp, OsmUser user, long changesetId) {
		// Chain to the more specific constructor
		this(id, version, new SimpleTimestampContainer(timestamp), user, changesetId);
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
	 */
	public CommonEntityData(
			long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId) {
		init(id, timestampContainer, user, version, changesetId);
		tags = new TagCollectionImpl();
		metaTags = new LazyHashMap<String, Object>();
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
	 */
	public CommonEntityData(
			long id, int version, Date timestamp, OsmUser user, long changesetId, Collection<Tag> tags) {
		// Chain to the more specific constructor
		this(id, version, new SimpleTimestampContainer(timestamp), user, changesetId, tags);
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
	 */
	public CommonEntityData(long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId,
			Collection<Tag> tags) {
		init(id, timestampContainer, user, version, changesetId);
		this.tags = new TagCollectionImpl(tags);
		metaTags = new LazyHashMap<String, Object>();
	}


	/**
	 * Initializes non-collection attributes.
	 * 
	 * @param newId
	 *            The unique identifier.
	 * @param newTimestampContainer
	 *            The container holding the timestamp in an alternative timestamp representation.
	 * @param newUser
	 *            The user that last modified this entity.
	 * @param newVersion
	 *            The version of the entity.
	 * @param changesetId
	 *            The id of the changeset that this version of the entity was created by.
	 */
	private void init(long newId, TimestampContainer newTimestampContainer, OsmUser newUser, int newVersion,
			long newChangesetId) {
		this.id = newId;
		this.timestampContainer = newTimestampContainer;
		this.user = newUser;
		this.version = newVersion;
		this.changesetId = LongAsInt.longToInt(newChangesetId);
	}
	
	
	private static TimestampContainer readTimestampContainer(StoreReader sr, StoreClassRegister scr) {
		if (sr.readBoolean()) {
			return new SimpleTimestampContainer(new Date(sr.readLong()));
		} else {
			return null;
		}
	}
	
	
	private static OsmUser readOsmUser(StoreReader sr, StoreClassRegister scr) {
		OsmUser user;
		
		// We could follow the same approach as timestamps and use a boolean to
		// indicate the existence of a user or not. But most entities have a
		// user so this would increase the space consumed on disk. So I'm taking
		// a small hit in instantiating unnecessary OsmUser objects when no user
		// is available.
		user = new OsmUser(sr, scr);
		
		if (user.equals(OsmUser.NONE)) {
			return OsmUser.NONE;
		} else {
			return user;
		}
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
	public CommonEntityData(StoreReader sr, StoreClassRegister scr) {
		this(
			sr.readLong(),
			sr.readInteger(),
			readTimestampContainer(sr, scr),
			readOsmUser(sr, scr),
			sr.readInteger(),
			new TagCollectionImpl(sr, scr)
		);
		
		int metaTagCount;
		
		metaTagCount = sr.readInteger();
		metaTags = new LazyHashMap<String, Object>();
		for (int i = 0; i < metaTagCount; i++) {
			metaTags.put(sr.readString(), sr.readString());
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeLong(id);
		
		sw.writeInteger(version);
		
		if (getTimestamp() != null) {
			sw.writeBoolean(true);
			sw.writeLong(timestampContainer.getTimestamp().getTime());
		} else {
			sw.writeBoolean(false);
		}
		
		user.store(sw, scr);
		
		sw.writeInteger(changesetId);
		
		tags.store(sw, scr);
		
		sw.writeInteger(metaTags.size());
		for (Entry<String, Object> tag : metaTags.entrySet()) {
			sw.writeString(tag.getKey());
			sw.writeString(tag.getValue().toString());
		}
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
		List<Tag> tags1;
		List<Tag> tags2;
		
		tags1 = new ArrayList<Tag>(tags);
		tags2 = new ArrayList<Tag>(comparisonTags);
		
		Collections.sort(tags1);
		Collections.sort(tags2);
		
		// The list with the most tags is considered bigger.
		if (tags1.size() != tags2.size()) {
			return tags1.size() - tags2.size();
		}
		
		// Check the individual tags.
		for (int i = 0; i < tags1.size(); i++) {
			int result = tags1.get(i).compareTo(tags2.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Gets the identifier.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}


	/**
	 * Sets the identifier.
	 * 
	 * @param id
	 *            The identifier.
	 */
	public void setId(long id) {
		assertWriteable();
		
		this.id = id;
	}
	
	
	/**
	 * Gets the version.
	 * 
	 * @return The version.
	 */
	public int getVersion() {
		return version;
	}


	/**
	 * Sets the version.
	 * 
	 * @param version
	 *            The version.
	 */
	public void setVersion(int version) {
		assertWriteable();
		
		this.version = version;
	}
	
	
	/**
	 * Gets the timestamp in date form. This is the standard method for
	 * retrieving timestamp information.
	 * 
	 * @return The timestamp.
	 */
	public Date getTimestamp() {
		return timestampContainer.getTimestamp();
	}


	/**
	 * Sets the timestamp in date form. This is the standard method of updating a timestamp.
	 * 
	 * @param timestamp
	 *            The timestamp.
	 */
	public void setTimestamp(Date timestamp) {
		assertWriteable();
		
		timestampContainer = new SimpleTimestampContainer(timestamp);
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
		return timestampContainer;
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
		assertWriteable();
		
		this.timestampContainer = timestampContainer;
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
		return timestampContainer.getFormattedTimestamp(timestampFormat);
	}
	
	
	/**
	 * Returns the user who last edited the entity.
	 * 
	 * @return The user.
	 */
	public OsmUser getUser() {
		return user;
	}
	
	
	/**
	 * Sets the last modification user.
	 * 
	 * @param user
	 *            The user.
	 */
	public void setUser(OsmUser user) {
		assertWriteable();
		
		this.user = user;
	}
	
	
	/**
	 * Gets the id of the changeset that this version of the entity was created by.
	 * 
	 * @return The changeset id.
	 */
	public long getChangesetId() {
		return changesetId;
	}
	
	
	/**
	 * Sets the id of the changeset that this version of the entity was created by.
	 * 
	 * @param changesetId
	 *            The changeset id.
	 */
	public void setChangesetId(long changesetId) {
		assertWriteable();
		
		this.changesetId = LongAsInt.longToInt(changesetId);
	}


	/**
	 * Returns the attached tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The tags.
	 */
	public Collection<Tag> getTags() {
		return tags;
	}


	/**
	 * Returns the attached meta tags. If the class is read-only, the collection will
	 * be read-only.
	 * 
	 * @return The metaTags.
	 */
	public Map<String, Object> getMetaTags() {
		return metaTags;
	}


	/**
	 * Indicates if the object has been set to read-only. A read-only object
	 * must be cloned in order to make updates. This allows objects shared
	 * between multiple threads to be locked for thread safety.
	 * 
	 * @return True if the object is read-only.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}


	/**
	 * Ensures that the object is writeable. If not an exception will be thrown.
	 * This is intended to be called within all update methods.
	 */
	protected void assertWriteable() {
		if (readOnly) {
			throw new OsmosisRuntimeException(
					"The object has been marked as read-only.  It must be cloned to make changes.");
		}
	}


	/**
	 * Configures the object to be read-only. This should be called if the object is to be processed
	 * by multiple threads concurrently. It updates the read-only status of the object, and makes
	 * all collections unmodifiable. This must be overridden by sub-classes to make their own
	 * collections unmodifiable.
	 */
	public void makeReadOnly() {
		if (!readOnly) {
			tags = new UnmodifiableTagCollection(tags);
			metaTags = Collections.unmodifiableMap(metaTags);
			
			readOnly = true;
		}
	}


	/**
	 * Returns a writable instance of this object. If the object is read-only a clone is created,
	 * if it is already writable then this object is returned.
	 * 
	 * @return A writable instance of this object.
	 */
	public CommonEntityData getWriteableInstance() {
		if (isReadOnly()) {
			return new CommonEntityData(id, version, timestampContainer, user, changesetId, tags);
		} else {
			return this;
		}
	}
}
