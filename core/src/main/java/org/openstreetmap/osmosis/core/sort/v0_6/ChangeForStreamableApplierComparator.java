// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;


/**
 * Orders changes in such a way that they can be applied to an ordered data
 * stream without requiring seeking throughout the data stream. (ie. an xml
 * dump). The change action to be performed (eg. Create) doesn't affect the sort
 * order. The changes are ordered as follows:
 * <ul>
 * <li>Nodes ordered by id and version</li>
 * <li>Ways ordered by id and version</li>
 * <li>Relations ordered by id and version</li>
 * </ul>
 * 
 * @author Brett Henderson
 */
public class ChangeForStreamableApplierComparator implements Comparator<ChangeContainer> {
	private Comparator<ChangeContainer> comparator;
	
	
	/**
	 * Creates a new instance.
	 */
	public ChangeForStreamableApplierComparator() {
		// We have an existing entity comparator that performs the same ordering so simply adapt it.
		comparator = new ChangeAsEntityComparator(new EntityContainerComparator(
				new EntityByTypeThenIdThenVersionComparator()));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ChangeContainer o1, ChangeContainer o2) {
		return comparator.compare(o1, o2);
	}
}
