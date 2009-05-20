// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.util;

import java.util.Collection;
import java.util.Iterator;


/**
 * Wraps an existing collection and delegates all calls to it. This is not useful on its own but can
 * be extended as necessary by classes that delegate most functionality to an underlying collection
 * implementation but extend or modify it in some ways.
 * 
 * @author Brett Henderson
 * 
 * @param <E> The type of data stored within the collection.
 */
public class CollectionWrapper<E> implements Collection<E> {

	
	private Collection<E> wrappedCollection;


	/**
	 * Creates a new instance.
	 * 
	 * @param wrappedCollection
	 *            The collection to be wrapped. This collection will not be copied, it will be used
	 *            directly.
	 */
	public CollectionWrapper(Collection<E> wrappedCollection) {
		this.wrappedCollection = wrappedCollection;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean add(E e) {
		return wrappedCollection.add(e);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean addAll(Collection<? extends E> c) {
		return wrappedCollection.addAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public void clear() {
		wrappedCollection.clear();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean contains(Object o) {
		return wrappedCollection.contains(o);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean containsAll(Collection<?> c) {
		return wrappedCollection.containsAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean isEmpty() {
		return wrappedCollection.isEmpty();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public Iterator<E> iterator() {
		return wrappedCollection.iterator();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean remove(Object o) {
		return wrappedCollection.remove(o);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean removeAll(Collection<?> c) {
		return wrappedCollection.removeAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean retainAll(Collection<?> c) {
		return wrappedCollection.retainAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public int size() {
		return wrappedCollection.size();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public Object[] toArray() {
		return wrappedCollection.toArray();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public <T> T[] toArray(T[] a) {
		return wrappedCollection.toArray(a);
	}
}
