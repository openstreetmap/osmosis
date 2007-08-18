package com.bretth.osmosis.extract.mysql;

import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SimpleObjectStore;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	
	private static final int RECORD_COUNT = 500000;
	
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		SimpleObjectStore<String> store;
		ReleasableIterator<String> iterator;
		
		store = new SimpleObjectStore<String>("bhtest", true);
		
		for (int i = 0; i < RECORD_COUNT; i++) {
			store.add(new String("test" + i));
		}
		
		iterator = store.iterate();
		
		int resultCount = 0;
		while (iterator.hasNext()) {
			iterator.next();
			resultCount++;
		}
		
		if (resultCount != RECORD_COUNT) {
			throw new RuntimeException(
				"Received " + resultCount + " instead of " + RECORD_COUNT + " records."
			);
		}
		iterator.release();
		store.release();
		
		System.out.println("done");
	}

}
