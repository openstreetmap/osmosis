package com.bretth.osmosis.sort;

import java.util.Comparator;


/**
 * Orders changes in such a way that they can be applied to an ordered data
 * stream without requiring seeking throughout the data stream. (ie. an xml
 * dump). The change action to be performed (eg. Create) doesn't affect the sort
 * order. The changes are ordered as follows:
 * <ul>
 * <li>Nodes ordered by id</li>
 * <li>Segments ordered by id</li>
 * <li>Ways ordered by id</li>
 * </ul>
 * 
 * @author Brett Henderson
 */
public class ChangeForStreamableApplierComparator implements Comparator<ChangeElement> {
	private ElementByTypeThenIdComparator comparator = new ElementByTypeThenIdComparator();
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ChangeElement o1, ChangeElement o2) {
		// Changes aren't involved, so we can delegate directly to a standard
		// element comparator.
		return comparator.compare(o1.getElement(), o2.getElement());
	}
}