// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import java.io.BufferedWriter;
import java.util.Collection;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a relation as xml.
 * 
 * @author Brett Henderson
 */
public class RelationWriter extends ElementWriter {
	private RelationMemberWriter relationMemberWriter;
	private TagWriter tagWriter;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public RelationWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		tagWriter = new TagWriter("tag", indentLevel + 1);
		relationMemberWriter = new RelationMemberWriter("member", indentLevel + 1);
	}
	
	
	/**
	 * Writes the relation.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	public void process(Relation relation) {
		OsmUser user;
		List<RelationMember> relationMembers;
		Collection<Tag> tags;
		
		user = relation.getUser();
		
		beginOpenElement();
		addAttribute("id", Long.toString(relation.getId()));
		addAttribute("version", Integer.toString(relation.getVersion()));
		addAttribute("timestamp", relation.getFormattedTimestamp(getTimestampFormat()));
		
		if (!user.equals(OsmUser.NONE)) {
			addAttribute("uid", Integer.toString(user.getId()));
			addAttribute("user", user.getName());
		}
		
		relationMembers = relation.getMembers();
		tags = relation.getTags();
		
		if (relationMembers.size() > 0 || tags.size() > 0) {
			endOpenElement(false);

			for (RelationMember relationMember : relationMembers) {
				relationMemberWriter.processRelationMember(relationMember);
			}
			
			for (Tag tag : tags) {
				tagWriter.process(tag);
			}
			
			closeElement();
			
		} else {
			endOpenElement(true);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWriter(BufferedWriter writer) {
		super.setWriter(writer);
		
		relationMemberWriter.setWriter(writer);
		tagWriter.setWriter(writer);
	}
}
