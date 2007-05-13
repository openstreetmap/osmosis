package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * A data class representing a single OSM segment.
 * 
 * @author Brett Henderson
 */
public class Segment {
	private long id;
	private long from;
	private long to;
	private List<Tag> tagList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param from
	 *            The id of the node marking the beginning of the segment.
	 * @param to
	 *            The id of the node marking the end of the segment.
	 */
	public Segment(long id, long from, long to) {
		this.id = id;
		this.from = from;
		this.to = to;
		
		tagList = new ArrayList<Tag>();
	}
	
	
	/**
	 * @return The id. 
	 */
	public long getId() {
		return id;
	}
	
	
	/**
	 * @return The from. 
	 */
	public long getFrom() {
		return from;
	}
	
	
	/**
	 * @return The to. 
	 */
	public long getTo() {
		return to;
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
}
