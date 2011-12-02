// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.util.Map.Entry;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.xml.common.ElementWriter;


/**
 * Provides common functionality for all classes writing OSM entities to xml.
 * 
 * @author Brett Henderson
 */
public class EntityWriter extends ElementWriter {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentionLevel
	 *            The indent level of the element.
	 */
	protected EntityWriter(String elementName, int indentionLevel) {
		super(elementName, indentionLevel);
	}


	/**
	 * Add common entity attributes.
	 * 
	 * @param entity
	 *            The entity being written.
	 */
	protected void addCommonAttributes(Entity entity) {
		addAttribute("id", Long.toString(entity.getId()));
		addAttribute("version", Integer.toString(entity.getVersion()));
		addAttribute("timestamp", entity.getFormattedTimestamp(getTimestampFormat()));

		OsmUser user = entity.getUser();
		if (!user.equals(OsmUser.NONE)) {
			addAttribute("uid", Integer.toString(user.getId()));
			addAttribute("user", user.getName());
		}

		if (entity.getChangesetId() != 0) {
			addAttribute("changeset", Long.toString(entity.getChangesetId()));
		}
	}


	/**
	 * Add metatag attributes.
	 * 
	 * @param entity
	 *            The entity being written.
	 */
	protected void addMetatags(Entity entity) {
		for (Entry<String, Object> metaTag : entity.getMetaTags().entrySet()) {
			addAttribute(metaTag.getKey(), metaTag.getValue().toString());
		}
	}
}
