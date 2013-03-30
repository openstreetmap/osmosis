// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagfilter.v0_6;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Filters a set of tags from all entities. This allows unwanted tags to be
 * removed from the data.
 * 
 * @author Jochen Topf
 * @author Brett Henderson
 */
public class TagRemover implements SinkSource {
	private Sink sink;
	private HashSet<String> keysToDrop;
	private String[] keyPrefixesToDrop;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyList
	 *            Comma separated list of keys of tags to be removed.
	 * @param keyPrefixList
	 *            Comma separated list of key prefixes of tags to be removed.
	 */
	public TagRemover(String keyList, String keyPrefixList) {
		keysToDrop = new HashSet<String>();
		String[] keys = keyList.split(",");
		for (int i = 0; i < keys.length; i++) {
			keysToDrop.add(keys[i]);
		}
		keyPrefixesToDrop = keyPrefixList.split(",");
		if (keyPrefixesToDrop[0] == "") {
			keyPrefixesToDrop = new String[] {};
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
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
	@Override
	public void process(EntityContainer entityContainer) {
		EntityContainer writeableContainer;
		Entity entity;
		
		writeableContainer = entityContainer.getWriteableInstance();
		entity = writeableContainer.getEntity();
		
		for (Iterator<Tag> i = entity.getTags().iterator(); i.hasNext();) {
			Tag tag;
			
			tag = i.next();
			
			if (keysToDrop.contains(tag.getKey())) {
				i.remove();
			} else {
				for (String prefix : keyPrefixesToDrop) {
					if (tag.getKey().startsWith(prefix)) {
						i.remove();
					   	break;
					}
				}
			}
		}
		
		sink.process(writeableContainer);
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
}
