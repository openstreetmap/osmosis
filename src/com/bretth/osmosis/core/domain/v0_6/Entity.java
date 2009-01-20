// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampFormat;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.util.IntAsChar;
import com.bretth.osmosis.core.util.LongAsInt;


/**
 * A data class representing a single OSM entity. All top level data types
 * inherit from this class.
 * 
 * @author Brett Henderson
 */
public abstract class Entity implements Storeable {
	private int id;
	private int version;
	private TimestampContainer timestampContainer;
	private OsmUser user;
	private Collection<Tag> tags;
	
	
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
	 * @param tags
	 *            The tags to apply to the object.
	 */
	public Entity(long id, int version, Date timestamp, OsmUser user, Collection<Tag> tags) {
		// Chain to the more specific constructor
		this(id, new SimpleTimestampContainer(timestamp), user, version, tags);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 * @param version
	 *            The version of the entity.
	 * @param tags
	 *            The tags to apply to the object.
	 */
	public Entity(long id, TimestampContainer timestampContainer, OsmUser user, int version, Collection<Tag> tags) {
		this.id = LongAsInt.longToInt(id);
		this.timestampContainer = timestampContainer;
		this.user = user;
		this.version = version;
		this.tags = Collections.unmodifiableCollection(new ArrayList<Tag>(tags));
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
		int tagCount;
		Collection<Tag> tmpTags;
		
		id = sr.readInteger();
		
		version = sr.readCharacter(); // store as a character for now, may need to be an int later
		
		if (sr.readBoolean()) {
			timestampContainer = new SimpleTimestampContainer(new Date(sr.readLong()));
		}
		
		user = new OsmUser(sr, scr);
		
		tagCount = sr.readCharacter();
		tmpTags = new ArrayList<Tag>(tagCount);
		for (int i = 0; i < tagCount; i++) {
			tmpTags.add(new Tag(sr, scr));
		}
		tags = Collections.unmodifiableCollection(tmpTags);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeInteger(id);
		
		sw.writeCharacter(IntAsChar.intToChar(version));
		
		if (getTimestamp() != null) {
			sw.writeBoolean(true);
			sw.writeLong(timestampContainer.getTimestamp().getTime());
		} else {
			sw.writeBoolean(false);
		}
		
		user.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(tags.size()));
		for (Tag tag : tags) {
			tag.store(sw, scr);
		}
	}
	
	
	/**
	 * Compares the tags on this entity to the specified tags. The tag
	 * comparison is based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTags
	 *            The tags to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
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
	 * Returns the specific data type represented by this entity.
	 * 
	 * @return The entity type enum value.
	 */
	public abstract EntityType getType();
	
	
	/**
	 * @return The id. 
	 */
	public long getId() {
		return id;
	}
	
	
	/**
	 * @return The version.
	 */
	public int getVersion() {
		return version;
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
	 * Returns the attached tags. The returned collection is read-only.
	 * 
	 * @return The tagList.
	 */
	public Collection<Tag> getTags() {
		return tags;
	}
}
