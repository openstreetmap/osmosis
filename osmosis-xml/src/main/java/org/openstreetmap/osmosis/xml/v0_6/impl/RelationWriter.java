// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Renders a relation as xml.
 *
 * @author Brett Henderson
 */
public class RelationWriter extends EntityWriter {
    /**
     * Write the ordered list of members of a relation.
     */
    private RelationMemberWriter relationMemberWriter;
    /**
     * Write the tags of a relation.
     */
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
		List<RelationMember> relationMembers;
		Collection<Tag> tags;
		
		beginOpenElement();
		addCommonAttributes(relation);
		addMetatags(relation);
		
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
	public void setWriter(final Writer writer) {
		super.setWriter(writer);
		
		relationMemberWriter.setWriter(writer);
		tagWriter.setWriter(writer);
	}
}
