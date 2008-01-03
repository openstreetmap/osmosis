// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_5;

import java.util.Comparator;

import com.bretth.osmosis.core.container.v0_5.ChangeContainer;


/**
 * Orders changes in such a way that they can be applied to an ordered data
 * stream without requiring seeking throughout the data stream. (ie. an xml
 * dump). The change action to be performed (eg. Create) doesn't affect the sort
 * order. The changes are ordered as follows:
 * <ul>
 * <li>Nodes ordered by id</li>
 * <li>Ways ordered by id</li>
 * <li>Relations ordered by id</li>
 * </ul>
 * 
 * @author Brett Henderson
 */
public class ChangeForStreamableApplierComparator implements Comparator<ChangeContainer> {
	private EntityByTypeThenIdComparator comparator = new EntityByTypeThenIdComparator();
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ChangeContainer o1, ChangeContainer o2) {
		// Changes aren't involved, so we can delegate directly to a standard
		// entity comparator.
		return comparator.compare(o1.getEntityContainer(), o2.getEntityContainer());
	}
}
