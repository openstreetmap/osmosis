package com.bretth.osm.conduit.sort;

import java.util.Comparator;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.OsmElement;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.Way;


/**
 * Compares two elements and sorts them first by their type (Nodes, then
 * Segments, then Ways) and then by their identifier.
 * 
 * @author Brett Henderson
 */
public class ElementByTypeThenIdComparator implements Comparator<OsmElement> {
	
	/**
	 * Allocates a score to an element based upon its type for sorting purposes.
	 * Sort order is nodes, followed by segments, followed by ways.
	 * 
	 * @param element
	 *            The element to be evaluated.
	 * @return The score.
	 */
	private int calculateTypeScore(OsmElement element) {
		if (element instanceof Node) {
			return 1;
		}
		if (element instanceof Segment) {
			return 2;
		}
		if (element instanceof Way) {
			return 3;
		}
		
		throw new ConduitRuntimeException("Element type " + element.getClass().getName() + " is not recognized.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(OsmElement o1, OsmElement o2) {
		int o1TypeScore;
		int o2TypeScore;
		long idDiff;
		
		// Perform a type comparison.
		o1TypeScore = calculateTypeScore(o1);
		o2TypeScore = calculateTypeScore(o2);
		if (o1TypeScore != o2TypeScore) {
			return o1TypeScore - o2TypeScore;
		}
		
		// Perform an identifier comparison.
		idDiff = o1.getId() - o2.getId();
		if (idDiff > 0) {
			return 1;
		}
		if (idDiff < 0) {
			return -1;
		}
		return 0;
	}
}
