package com.bretth.osm.conduit.sort;

import java.util.Comparator;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.ChangeAction;


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
		OsmElement element = changeElement.getElement();
		
		if (element instanceof Node) {
			if (action.equals(ChangeAction.Create)) {
				return 1;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 6;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 9;
			}
		} else if (element instanceof Segment) {
			if (action.equals(ChangeAction.Create)) {
				return 2;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 5;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 8;
			}
		} else if (element instanceof Way) {
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
		
		throw new ConduitRuntimeException(
			"The change element with action " + action
			+ " and element " + element
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
