package com.bretth.osmosis.core.domain.v0_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.domain.common.Entity;
import com.bretth.osmosis.core.domain.common.EntityType;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Entity implements Comparable<Way> {
	private static final long serialVersionUID = 1L;
	
	
	private List<NodeReference> nodeReferenceList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The name of the user that last modified this entity.
	 */
	public Way(long id, Date timestamp, String user) {
		super(id, timestamp, user);
		
		nodeReferenceList = new ArrayList<NodeReference>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Way;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Way) {
			return compareTo((Way) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Compares this node list to the specified node list. The comparison is
	 * based on a direct comparison of the node ids.
	 * 
	 * @param comparisonNodeReferenceList
	 *            The node reference list to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	protected int compareNodeReferences(List<NodeReference> comparisonNodeReferenceList) {
		List<NodeReference> list1;
		List<NodeReference> list2;
		
		list1 = new ArrayList<NodeReference>(nodeReferenceList);
		list2 = new ArrayList<NodeReference>(comparisonNodeReferenceList);
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		// The list with the most entities is considered bigger.
		if (list1.size() != list2.size()) {
			return list1.size() - list2.size();
		}
		
		// Check the individual node references.
		for (int i = 0; i < list1.size(); i++) {
			int result = list1.get(i).compareTo(list2.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this way to the specified way. The way comparison is based on a
	 * comparison of id, timestamp, segmentReferenceList and tags in that order.
	 * 
	 * @param comparisonWay
	 *            The way to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Way comparisonWay) {
		int nodeReferenceListResult;
		
		if (this.getId() < comparisonWay.getId()) {
			return -1;
		}
		if (this.getId() > comparisonWay.getId()) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonWay.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonWay.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		nodeReferenceListResult = compareNodeReferences(
			comparisonWay.getNodeReferenceList()
		);
		
		if (nodeReferenceListResult != 0) {
			return nodeReferenceListResult;
		}
		
		return compareTags(comparisonWay.getTagList());
	}
	
	
	/**
	 * Returns the attached list of node references. The returned list is
	 * read-only.
	 * 
	 * @return The nodeReferenceList.
	 */
	public List<NodeReference> getNodeReferenceList() {
		return Collections.unmodifiableList(nodeReferenceList);
	}
	
	
	/**
	 * Adds a new node reference.
	 * 
	 * @param nodeReference
	 *            The segment reference to add.
	 */
	public void addNodeReference(NodeReference nodeReference) {
		nodeReferenceList.add(nodeReference);
	}
	
	
	/**
	 * Adds all node references in the collection to the node.
	 * 
	 * @param nodeReferences
	 *            The collection of node references to be added.
	 */
	public void addNodeReferences(Collection<NodeReference> nodeReferences) {
		nodeReferenceList.addAll(nodeReferences);
	}
}
