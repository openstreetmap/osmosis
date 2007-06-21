package com.bretth.osm.osmosis.sort;

import java.util.Comparator;

import com.bretth.osm.osmosis.OsmosisRuntimeException;
import com.bretth.osm.osmosis.data.Element;
import com.bretth.osm.osmosis.data.ElementType;
import com.bretth.osm.osmosis.task.ChangeAction;


/**
 * Orders changes in such a way that they can be applied to a data store while
 * maintaining data integrity (ie. a database). For example, the ordering
 * prevents a way being added before the underlying nodes are created. The
 * changes are ordered as follows:
 * <ul>
 * <li>Node creation</li>
 * <li>Segment creation</li>
 * <li>Way creation</li>
 * <li>Way modification</li>
 * <li>Segment modification</li>
 * <li>Node modification</li>
 * <li>Way deletion</li>
 * <li>Segment deletion</li>
 * <li>Node deletion</li>
 * </ul>
 * 
 * @author Brett Henderson
 */
public class ChangeForSeekableApplierComparator implements Comparator<ChangeElement> {
	
	/**
	 * Create a weighting for the change. The weighting is the index into the
	 * sorting list implemented by this class.
	 * 
	 * @param changeElement
	 *            The change to be analysed.
	 * @return The sort weighting.
	 */
	private int calculateSortWeight(ChangeElement changeElement) {
		ChangeAction action = changeElement.getAction();
		Element element = changeElement.getElement();
		
		if (element.getElementType().equals(ElementType.Node)) {
			if (action.equals(ChangeAction.Create)) {
				return 1;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 6;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 9;
			}
		} else if (element.getElementType().equals(ElementType.Segment)) {
			if (action.equals(ChangeAction.Create)) {
				return 2;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 5;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 8;
			}
		} else if (element.getElementType().equals(ElementType.Way)) {
			if (action.equals(ChangeAction.Create)) {
				return 3;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 4;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 7;
			}
		}
		
		throw new OsmosisRuntimeException(
			"The change element with action " + action
			+ " type " + element.getElementType()
			+ " and id " + element.getId()
			+ " was not recognised."
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ChangeElement o1, ChangeElement o2) {
		return calculateSortWeight(o1) - calculateSortWeight(o2);
	}

}
