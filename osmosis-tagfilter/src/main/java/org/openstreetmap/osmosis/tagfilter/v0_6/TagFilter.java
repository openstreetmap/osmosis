// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.util.Set;
import java.util.Map;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

/**
 * A simple class to filter node, way, and relation entities by their tag keys and/or values.
 * 
 * @author Andrew Byrd
 */
public class TagFilter implements SinkSource {
    private Sink sink;
    private Set<String> tagKeys;
    private Map<String, Set<String>> tagKeyValues;
    private Class<? extends EntityContainer> filterClass;
    private boolean reject;
    private boolean matchesEverything;
    private static final Logger LOG = Logger.getLogger(TagFilter.class.getName());
    

    /**
     * Creates a new instance. 
     *
     * @param filterMode
     *          A 2-field dash-separated string specifying:
     *          1. Whether the filter accepts or rejects entities
     *          2. The entity type upon which it operates
     *
     * @param tagKeys
     *          A Set of tag key Strings. The filter will match these tags irrespective of tag values.
     *
     * @param tagKeyValues
     *          A map of tag key Strings to Sets of tag key values. These key-value pairs are checked 
     *          against each entity's tags to determine whether or not it matches the filter.
     */
    public TagFilter(String filterMode, Set<String> tagKeys, Map<String, Set<String>> tagKeyValues) {        
        String[] filterModeSplit = filterMode.toLowerCase().split("-");
        if (filterModeSplit.length != 2) { 
            throw new OsmosisRuntimeException(
            "The TagFilter task's default parameter must consist of an action and an entity type separated by '-'."); 
        }

        String action = filterModeSplit[0];
        if (action.equals("accept")) { 
            reject = false; 
        } else if (action.equals("reject")) { 
            reject = true;  
        } else { 
            throw new OsmosisRuntimeException(
            "The TagFilter action must be either 'accept' or 'reject'. '" + action + "' is not a supported mode."); 
        }

        String entity = filterModeSplit[1];
        if  (entity.endsWith("s")) { 
            entity = entity.substring(0, entity.length() - 1); 
        }
        if  (entity.equals("node")) { 
            filterClass = NodeContainer.class; 
        } else if (entity.equals("way")) { 
            filterClass = WayContainer.class;      
        } else if (entity.equals("relation")) { 
            filterClass = RelationContainer.class; 
        } else { 
            throw new OsmosisRuntimeException(
            "The TagFilter entity type must be one of 'node', 'way', or 'relation'. '" + entity 
            + "' is not a supported entity type."); 
        }
        
        matchesEverything = (tagKeys.size() == 0 && tagKeyValues.size() == 0);
        this.tagKeys = tagKeys;
        this.tagKeyValues = tagKeyValues;
        
        String logString = "New TagFilter ";
        if (reject) {
            logString += "rejects ";
        } else {
            logString += "accepts ";
        }    
        logString += filterClass;
        if (matchesEverything) {
            logString += " (no tag-based filtering).";
        } else {
            logString += " having tag keys " + tagKeys + " or tag key-value pairs " + tagKeyValues + ".";
        }
        LOG.finer(logString);
    }


    /**
     * Checks whether the Entity in a container has tags that match this filter.
     *
     * @param container
     *      The container holding the entity whose tags shall be examined.
     */
    private boolean matches(EntityContainer container) {
        boolean matched = false;
        for (Tag tag : container.getEntity().getTags()) {
            String key = tag.getKey();
            if (tagKeys.contains(key)) {
                matched = true;
                break; 
            }
            Set<String> values = tagKeyValues.get(key);
            if ((values != null) && values.contains(tag.getValue())) {
                matched = true;
                break; 
            } 
        }
        return matched;    
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void initialize(Map<String, Object> metaData) {
		sink.initialize(metaData);
	}


    /**
     * {@inheritDoc}
     */
    public void process(EntityContainer container) {
        if (filterClass.isInstance(container)) {
            if (reject ^ (matchesEverything || matches(container))) {
                sink.process(container);
            }
        } else {
            sink.process(container);
        }
    }
        

    /**
     * {@inheritDoc}
     */
    public void complete() {
        sink.complete();
    }


    /**
     * {@inheritDoc}
     */
    public void release() {
        sink.release();
    }


    /**
     * {@inheritDoc}
     */
    public void setSink(Sink sink) {
        this.sink = sink;
    }
}
