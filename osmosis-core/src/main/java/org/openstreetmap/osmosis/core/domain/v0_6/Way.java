// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.IntAsChar;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Entity implements Comparable<Way> {
	
	private List<WayNode> wayNodes;
	
	
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
	 * @deprecated As of 0.40, replaced by Way(entityData).
	 */
	public Way(long id, int version, Date timestamp, OsmUser user, long changesetId) {
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
	 *            The name of the user that last modified this entity.
	 * @param changesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @deprecated As of 0.40, replaced by Way(entityData).
	 */
	public Way(long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId) {
		super(id, version, timestampContainer, user, changesetId);
		
		this.wayNodes = new ArrayList<WayNode>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 */
	public Way(CommonEntityData entityData) {
		super(entityData);
		
		this.wayNodes = new ArrayList<WayNode>();
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
	 * @param wayNodes
	 *            The way nodes to apply to the object
	 * @deprecated As of 0.40, replaced by Way(entityData, wayNodes).
	 */
	public Way(long id, int version, Date timestamp, OsmUser user, long changesetId, Collection<Tag> tags,
			List<WayNode> wayNodes) {
		// Chain to the more specific constructor
		this(id, version, new SimpleTimestampContainer(timestamp), user, changesetId, tags, wayNodes);
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
	 *            The name of the user that last modified this entity.
	 * @param changesetId
	 *            The id of the changeset that this version of the entity was created by.
	 * @param tags
	 *            The tags to apply to the object.
	 * @param wayNodes
	 *            The way nodes to apply to the object
	 * @deprecated As of 0.40, replaced by Way(entityData, wayNodes).
	 */
	public Way(
			long id, int version, TimestampContainer timestampContainer, OsmUser user, long changesetId,
			Collection<Tag> tags, List<WayNode> wayNodes) {
		super(id, version, timestampContainer, user, changesetId, tags);
		
		this.wayNodes = new ArrayList<WayNode>(wayNodes);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entityData
	 *            The common entity data.
	 * @param wayNodes
	 *            The way nodes to apply to the object
	 */
	public Way(
			CommonEntityData entityData, List<WayNode> wayNodes) {
		super(entityData);
		
		this.wayNodes = new ArrayList<WayNode>(wayNodes);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param originalWay
	 *            The way to clone from.
	 */
	private Way(Way originalWay) {
		super(originalWay);
		
		this.wayNodes = new ArrayList<WayNode>(originalWay.wayNodes);
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
	public Way(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		int featureCount;
		
		featureCount = sr.readCharacter();
		
		wayNodes = new ArrayList<WayNode>();
		for (int i = 0; i < featureCount; i++) {
			wayNodes.add(new WayNode(sr, scr));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(wayNodes.size()));
		for (WayNode wayNode : wayNodes) {
			wayNode.store(sw, scr);
		}
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using both id and version will provide a good distribution of
		 * values but is simple to calculate.
		 */
		return (int) getId() + getVersion();
	}


	/**
	 * Compares this node list to the specified node list. The comparison is
	 * based on a direct comparison of the node ids.
	 * 
	 * @param comparisonWayNodes
	 *            The node list to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareWayNodes(List<WayNode> comparisonWayNodes) {
		Iterator<WayNode> i;
		Iterator<WayNode> j;
		
		// The list with the most entities is considered bigger.
		if (wayNodes.size() != comparisonWayNodes.size()) {
			return wayNodes.size() - comparisonWayNodes.size();
		}
		
		// Check the individual way nodes.
		i = wayNodes.iterator();
		j = comparisonWayNodes.iterator();
		while (i.hasNext()) {
			int result = i.next().compareTo(j.next());
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this way to the specified way. The way comparison is based on a
	 * comparison of id, version, timestamp, wayNodeList and tags in that order.
	 * 
	 * @param comparisonWay
	 *            The way to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Way comparisonWay) {
		int wayNodeListResult;
		
		if (this.getId() < comparisonWay.getId()) {
			return -1;
		}
		if (this.getId() > comparisonWay.getId()) {
			return 1;
		}
		
		if (this.getVersion() < comparisonWay.getVersion()) {
			return -1;
		}
		if (this.getVersion() > comparisonWay.getVersion()) {
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
		
		wayNodeListResult = compareWayNodes(
			comparisonWay.getWayNodes()
		);
		
		if (wayNodeListResult != 0) {
			return wayNodeListResult;
		}
		
		return compareTags(comparisonWay.getTags());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void makeReadOnly() {
		if (!isReadOnly()) {
			wayNodes = Collections.unmodifiableList(wayNodes);
		}
		
		super.makeReadOnly();
	}


	/**
	 * Returns the attached list of way nodes. The returned list is read-only.
	 * 
	 * @return The wayNodeList.
	 */
	public List<WayNode> getWayNodes() {
		return wayNodes;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Way getWriteableInstance() {
		if (isReadOnly()) {
			return new Way(this);
		} else {
			return this;
		}
	}


    /**
     * Is this way closed? (A way is closed if the first node id equals the last node id.)
     *
     * @return True or false
     */
    public boolean isClosed() {
        return wayNodes.get(0).getNodeId() == wayNodes.get(wayNodes.size() - 1).getNodeId();
    }

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        String name = null;
        Collection<Tag> tags = getTags();
        for (Tag tag : tags) {
            if (tag.getKey() != null && tag.getKey().equalsIgnoreCase("name")) {
                name = tag.getValue();
                break;
            }
        }
        if (name != null) {
            return "Way(id=" + getId() + ", #tags=" +  getTags().size() + ", name='" + name + "')";
        }
        return "Way(id=" + getId() + ", #tags=" +  getTags().size() + ")";
    }
}
