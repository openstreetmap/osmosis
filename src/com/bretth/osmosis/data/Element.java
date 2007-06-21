package com.bretth.osmosis.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * A data class representing a single OSM element. All top level data types
 * inherit from this class.
 * 
 * @author Brett Henderson
 */
public abstract class Element implements Serializable {
	private long id;
	private List<Tag> tagList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 */
	public Element(long id) {
		this.id = id;
		
		tagList = new ArrayList<Tag>();
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
	 * Returns the specific data type represented by this element.
	 * 
	 * @return The element type enum value.
	 */
	public abstract ElementType getElementType();
	
	
	/**
	 * @return The id. 
	 */
	public long getId() {
		return id;
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
