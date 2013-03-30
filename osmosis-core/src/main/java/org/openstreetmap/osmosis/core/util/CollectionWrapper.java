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
	@Override
	public boolean add(E e) {
		return wrappedCollection.add(e);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		return wrappedCollection.addAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		wrappedCollection.clear();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		return wrappedCollection.contains(o);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return wrappedCollection.containsAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return wrappedCollection.isEmpty();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return wrappedCollection.iterator();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) {
		return wrappedCollection.remove(o);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return wrappedCollection.removeAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return wrappedCollection.retainAll(c);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return wrappedCollection.size();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return wrappedCollection.toArray();
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return wrappedCollection.toArray(a);
	}
}
