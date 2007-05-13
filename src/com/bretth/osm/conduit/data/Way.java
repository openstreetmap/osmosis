package com.bretth.osm.conduit.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way {
	private long id;
	private Date timestamp;
	private List<Tag> tagList;
	private List<SegmentReference> segmentReferenceList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 */
	public Way(long id, Date timestamp) {
		this.id = id;
		this.timestamp = timestamp;
		
		tagList = new ArrayList<Tag>();
		segmentReferenceList = new ArrayList<SegmentReference>();
	}
	
	
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
		return timestamp;
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
	 * Returns the attached list of segment references. The returned list is
	 * read-only.
	 * 
	 * @return The segmentReferenceList.
	 */
	public List<SegmentReference> getSegmentReferenceList() {
		return Collections.unmodifiableList(segmentReferenceList);
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
	 * Adds a new segment reference.
	 * 
	 * @param segmentReference
	 *            The segment reference to add.
	 */
	public void addSegmentReference(SegmentReference segmentReference) {
		segmentReferenceList.add(segmentReference);
	}
}
