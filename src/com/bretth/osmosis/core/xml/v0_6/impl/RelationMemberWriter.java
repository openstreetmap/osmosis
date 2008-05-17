// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.xml.common.ElementWriter;


/**
 * Renders a relation member as xml.
 * 
 * @author Brett Henderson
 */
public class RelationMemberWriter extends ElementWriter {
	
	private MemberTypeRenderer memberTypeRenderer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementName
	 *            The name of the element to be written.
	 * @param indentLevel
	 *            The indent level of the element.
	 */
	public RelationMemberWriter(String elementName, int indentLevel) {
		super(elementName, indentLevel);
		
		memberTypeRenderer = new MemberTypeRenderer();
	}
	
	
	/**
	 * Writes the way node.
	 * 
	 * @param relationMember
	 *            The wayNode to be processed.
	 */
	public void processRelationMember(RelationMember relationMember) {
		beginOpenElement();
		addAttribute("type", memberTypeRenderer.render(relationMember.getMemberType()));
		addAttribute("ref", Long.toString(relationMember.getMemberId()));
		addAttribute("role", relationMember.getMemberRole());
		endOpenElement(true);
	}
}
