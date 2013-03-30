// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.xml.sax.Attributes;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.xml.common.BaseElementProcessor;


/**
 * Provides an element processor implementation for a relation member.
 * 
 * @author Brett Henderson
 */
public class RelationMemberElementProcessor extends BaseElementProcessor {
	private static final String ATTRIBUTE_NAME_ID = "ref";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
	
	private RelationMemberListener relationMemberListener;
	private RelationMember relationMember;
	private MemberTypeParser memberTypeParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent element processor.
	 * @param relationMemberListener
	 *            The relation member listener for receiving created tags.
	 */
	public RelationMemberElementProcessor(
			BaseElementProcessor parentProcessor, RelationMemberListener relationMemberListener) {
		super(parentProcessor, true);
		
		this.relationMemberListener = relationMemberListener;
		
		memberTypeParser = new MemberTypeParser();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void begin(Attributes attributes) {
		long id;
		EntityType type;
		String role;
		
		id = Long.parseLong(attributes.getValue(ATTRIBUTE_NAME_ID));
		type = memberTypeParser.parse(attributes.getValue(ATTRIBUTE_NAME_TYPE));
		role = attributes.getValue(ATTRIBUTE_NAME_ROLE);
		if (role == null) {
			role = ""; // this may actually happen
		}
		
		relationMember = new RelationMember(id, type, role);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void end() {
		relationMemberListener.processRelationMember(relationMember);
		relationMember = null;
	}
}
