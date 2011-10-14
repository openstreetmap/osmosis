// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;


/**
 * Provides common element processor functionality for all entity processors.
 * 
 * @author Brett Henderson
 */
public abstract class EntityElementProcessor extends SourceElementProcessor {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 * @param sink
	 *            The sink for receiving processed data.
	 * @param enableDateParsing
	 *            If true, dates will be parsed from xml data, else the current
	 *            date will be used thus saving parsing time.
	 */
	public EntityElementProcessor(BaseElementProcessor parentProcessor, Sink sink, boolean enableDateParsing) {
		super(parentProcessor, sink, enableDateParsing);
	}
	
	
	/**
	 * Creates a user instance appropriate to the arguments. This includes
	 * identifying the case where no user is available.
	 * 
	 * @param rawUserId
	 *            The value of the user id attribute.
	 * @param rawUserName
	 *            The value of the user name attribute.
	 * @return The appropriate user instance.
	 */
	protected OsmUser buildUser(String rawUserId, String rawUserName) {
		if (rawUserId != null) {
			int userId;
			String userName;
			
			userId = Integer.parseInt(rawUserId);
			if (rawUserName == null) {
				userName = "";
			} else {
				userName = rawUserName;
			}
			
			return new OsmUser(userId, userName);
			
		} else {
			return OsmUser.NONE;
		}
	}
	
	
	/**
	 * Parses a changeset id based on the provided attribute value. If no attribute is available it
	 * will be defaulted to 0.
	 * 
	 * @param rawChangesetId
	 *            The raw changeset id attribute value.
	 * @return The parsed changeset id.
	 */
	protected long buildChangesetId(String rawChangesetId) {
		if (rawChangesetId != null) {
			return Long.parseLong(rawChangesetId);
		} else {
			return 0;
		}
	}
}
