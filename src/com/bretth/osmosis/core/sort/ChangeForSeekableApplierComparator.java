package com.bretth.osmosis.core.sort;

import java.util.Comparator;

import com.bretth.osmosis.core.container.ChangeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.data.Entity;
import com.bretth.osmosis.core.data.EntityType;
import com.bretth.osmosis.core.task.ChangeAction;


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
public class ChangeForSeekableApplierComparator implements Comparator<ChangeContainer> {
	
	/**
	 * Create a weighting for the change. The weighting is the index into the
	 * sorting list implemented by this class.
	 * 
	 * @param changeEntity
	 *            The change to be analysed.
	 * @return The sort weighting.
	 */
	private int calculateSortWeight(ChangeContainer changeEntity) {
		ChangeAction action = changeEntity.getAction();
		Entity entity = changeEntity.getEntityContainer().getEntity();
		
		if (entity.getType().equals(EntityType.Node)) {
			if (action.equals(ChangeAction.Create)) {
				return 1;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 6;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 9;
			}
		} else if (entity.getType().equals(EntityType.Segment)) {
			if (action.equals(ChangeAction.Create)) {
				return 2;
			}
			if (action.equals(ChangeAction.Modify)) {
				return 5;
			}
			if (action.equals(ChangeAction.Delete)) {
				return 8;
			}
		} else if (entity.getType().equals(EntityType.Way)) {
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
			"The change entity with action " + action
			+ " type " + entity.getType()
			+ " and id " + entity.getId()
			+ " was not recognised."
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ChangeContainer o1, ChangeContainer o2) {
		return calculateSortWeight(o1) - calculateSortWeight(o2);
	}

}
