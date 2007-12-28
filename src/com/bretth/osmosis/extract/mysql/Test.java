package com.bretth.osmosis.extract.mysql;

import java.util.Date;

import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.store.GenericObjectSerializationFactory;
import com.bretth.osmosis.core.store.IndexedObjectStore;
import com.bretth.osmosis.core.store.IndexedObjectStoreReader;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		long value;
		
		value = Integer.MAX_VALUE;
		
		System.out.println(value);
		System.out.println(toInt(value));
		System.out.println(toLong(toInt(value)));
		System.out.println();
		
		value++;
		
		System.out.println(value);
		System.out.println(toInt(value));
		System.out.println(toLong(toInt(value)));
		System.out.println();
		
		value++;
		
		System.out.println(value);
		System.out.println(toInt(value));
		System.out.println(toLong(toInt(value)));
		System.out.println();
		
		value++;
		
		System.out.println(value);
		System.out.println(toInt(value));
		System.out.println(toLong(toInt(value)));
		System.out.println();
		
		value++;
		
		System.out.println(value);
		System.out.println(toInt(value));
		System.out.println(toLong(toInt(value)));
		System.out.println();
		
		//IndexedObjectStore<Node> store = new IndexedObjectStore<Node>(new SingleClassObjectSerializationFactory(Node.class), "test");
		IndexedObjectStore<Node> store = new IndexedObjectStore<Node>(new GenericObjectSerializationFactory(), "test");
		
		try {
			IndexedObjectStoreReader<Node> storeReader;
			
			System.out.println("Start " + new Date());
			for (int i = 0; i < 10000; i++) {
				Node node;
				
				node = new Node(i, new Date(), "user" + i, 0, 0);
				for (int j = 0; j < 100; j++) {
					node.addTag(new Tag("key" + i, "This is the key value"));
				}
				
				store.add(i, node);
			}
			System.out.println("Middle " + new Date());
			
			storeReader = store.createReader();
			try {
				for (int i = 0; i < 10000; i++) {
					storeReader.get(i).getUser();
				}
			} finally {
				storeReader.release();
			}
			
			System.out.println("Finish " + new Date());
			
		} finally {
			store.release();
		}
	}
	
	
	private static int toInt(long value) {
		return (int) value;
	}
	
	
	private static long toLong(int value) {
		return value & 0xFFFFFFFFl;
	}
}
