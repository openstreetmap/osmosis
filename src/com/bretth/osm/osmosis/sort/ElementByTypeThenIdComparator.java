package com.bretth.osm.osmosis.sort;

import java.util.Comparator;

import com.bretth.osm.osmosis.data.Element;


/**
 * Compares two elements and sorts them first by their type (Nodes, then
 * Segments, then Ways) and then by their identifier.
 * 
 * @author Brett Henderson
 */
public class ElementByTypeThenIdComparator implements Comparator<Element> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(Element o1, Element o2) {
		int typeDiff;
		long idDiff;
		
		// Perform a type comparison.
		typeDiff = o1.getElementType().compareTo(o2.getElementType());
		if (typeDiff != 0) {
			return typeDiff;
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
