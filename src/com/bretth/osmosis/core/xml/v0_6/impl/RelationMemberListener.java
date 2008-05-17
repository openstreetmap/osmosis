// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_6.impl;

import com.bretth.osmosis.core.domain.v0_6.RelationMember;


/**
 * Provides the definition of a class receiving relation members.
 * 
 * @author Brett Henderson
 */
public interface RelationMemberListener {
	/**
	 * Processes the relation member.
	 * 
	 * @param relationMember
	 *            The relation member.
	 */
	void processRelationMember(RelationMember relationMember);
}
