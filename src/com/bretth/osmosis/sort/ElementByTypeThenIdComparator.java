package com.bretth.osmosis.sort;

import java.util.Comparator;

import com.bretth.osmosis.container.ElementContainer;


/**
 * Compares two elements and sorts them first by their type (Nodes, then
 * Segments, then Ways) and then by their identifier.
 * 
 * @author Brett Henderson
 */
public class ElementByTypeThenIdComparator implements Comparator<ElementContainer> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(ElementContainer o1, ElementContainer o2) {
		int typeDiff;
		long idDiff;
		
		// Perform a type comparison.
		typeDiff = o1.getElement().getElementType().compareTo(o2.getElement().getElementType());
		if (typeDiff != 0) {
			return typeDiff;
		}
		
		// Perform an identifier comparison.
		idDiff = o1.getElement().getId() - o2.getElement().getId();
		if (idDiff > 0) {
			return 1;
		}
		if (idDiff < 0) {
			return -1;
		}
		return 0;
	}
}
