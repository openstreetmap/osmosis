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
	private TimestampContainer timestampContainer;
	private List<Tag> tagList;
	private OsmUser user;
	private int version;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The name of the user that last modified this entity.
	 * @param userId
	 *            The unique identifier of the last user to modify this entry.
	 * @param version
	 *            The version of the entity.
	 */
	public Entity(long id, Date timestamp, String user, int userId, int version) {
		// Chain to the more specific constructor
		this(id, new SimpleTimestampContainer(timestamp), user, userId, version);
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
	 *            The name of the user that last modified this entity.
	 * @param userId
	 *            The unique identifier of the last user to modify this entry.
	 * @param version
	 *            The version of the entity.
	 */
	public Entity(long id, TimestampContainer timestampContainer, String user, int userId, int version) {
		this.id = LongAsInt.longToInt(id);
		this.timestampContainer = timestampContainer;
		this.user = OsmUser.getInstance(user, userId);
		this.version = version;
		
		tagList = new ArrayList<Tag>();
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
		
		id = sr.readInteger();
		if (sr.readBoolean()) {
			timestampContainer = new SimpleTimestampContainer(new Date(sr.readLong()));
		}
		
		tagCount = sr.readCharacter();
		tagList = new ArrayList<Tag>(tagCount);
		
		for (int i = 0; i < tagCount; i++) {
			addTag(new Tag(sr, scr));
		}
		
		user = OsmUser.getInstance(sr, scr);
		
		version = sr.readCharacter(); // store as a character for now, may need to be an int later
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeInteger(id);
		
		if (getTimestamp() != null) {
			sw.writeBoolean(true);
			sw.writeLong(timestampContainer.getTimestamp().getTime());
		} else {
			sw.writeBoolean(false);
		}
		
		sw.writeCharacter(IntAsChar.intToChar(tagList.size()));
		for (Tag tag : tagList) {
			tag.store(sw, scr);
		}
		
		user.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(version));
	}
	
	
	/**
	 * Compares this tag list to the specified tag list. The tag comparison is
	 * based on a comparison of key and value in that order.
	 * 
	 * @param comparisonTagList
	 *            The tagList to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	protected int compareTags(List<Tag> comparisonTagList) {
		List<Tag> tagList1;
		List<Tag> tagList2;
		
		tagList1 = new ArrayList<Tag>(tagList);
		tagList2 = new ArrayList<Tag>(comparisonTagList);
		
		Collections.sort(tagList1);
		Collections.sort(tagList2);
		
		// The list with the most tags is considered bigger.
		if (tagList1.size() != tagList2.size()) {
			return tagList1.size() - tagList2.size();
		}
		
		// Check the individual tags.
		for (int i = 0; i < tagList1.size(); i++) {
			int result = tagList1.get(i).compareTo(tagList2.get(i));
			
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
	 * @return The timestamp. 
	 */
	public Date getTimestamp() {
		return timestampContainer.getTimestamp();
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
	 * Returns the attached list of tags. The returned list is read-only.
	 * 
	 * @return The tagList.
	 */
	public List<Tag> getTagList() {
		return Collections.unmodifiableList(tagList);
	}
	
	
	/**
	 * Adds a new tag.
	 * 
	 * @param tag
	 *            The tag to add.
	 */
	public void addTag(Tag tag) {
		tagList.add(tag);
	}
	
	
	/**
	 * Adds all tags in the collection to the node.
	 * 
	 * @param tags
	 *            The collection of tags to be added.
	 */
	public void addTags(Collection<Tag> tags) {
		tagList.addAll(tags);
	}
	
	
	/**
	 * @return The userName. 
	 */
	public String getUserName() {
		return user.getUserName();
	}
	
	
	/**
	 * @return The userId.
	 */
	public int getUserId() {
		return user.getUserId();
	}


	/**
	 * @return The version.
	 */
	public int getVersion() {
		return version;
	}
}
