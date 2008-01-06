// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.extract.mysql;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.store.DataOutputStoreWriter;
import com.bretth.osmosis.core.store.ObjectWriter;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreWriter;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	
	private static void x(Node node, int count) throws Exception {
		EnvironmentConfig envConfig;
		Environment env;
		DatabaseConfig dbConfig;
		Database db;
		TupleBinding longBinding;
		StoreableTupleBinding<Node> nodeBinding;
		DatabaseEntry keyEntry;
		DatabaseEntry dataEntry;
		Date startDate;
		Date endDate;
		
		envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		
		env = new Environment(new File("c:/tmp/db"), envConfig);
		if (env.getDatabaseNames().contains("node")) {
			env.removeDatabase(null, "node");
		}
		
		dbConfig = new DatabaseConfig();
		dbConfig.setAllowCreate(true);
		dbConfig.setExclusiveCreate(true);
		
		db = env.openDatabase(null, "node", dbConfig);
		
		longBinding = TupleBinding.getPrimitiveBinding(Long.class);
		nodeBinding = new StoreableTupleBinding<Node>(Node.class);
		
		
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
		
		startDate = new Date();
		for (int i = 0; i < count; i++) {
			//node.id = i;
			
			longBinding.objectToEntry(node.getId(), keyEntry);
			nodeBinding.objectToEntry(node, dataEntry);
			
			db.put(null, keyEntry, dataEntry);
		}
		endDate = new Date();
		System.out.println("start: " + startDate);
		System.out.println("end: " + endDate);
		System.out.println("duration: " + (endDate.getTime() - startDate.getTime()) / 1000);
		
		db.close();
		
		env.close();
	}
	
	
	private static void y(Node node, int count) throws Exception {
		DataOutputStream dataStream;
		StoreWriter storeWriter;
		ObjectWriter objectWriter;
		Date startDate;
		Date endDate;
		
		dataStream = new DataOutputStream(new FileOutputStream("c:/tmp/datafile.txt"));
		storeWriter = new DataOutputStoreWriter(dataStream);
		objectWriter = new SingleClassObjectSerializationFactory(Node.class).createObjectWriter(storeWriter, new StoreClassRegister());
		
		startDate = new Date();
		for (int i = 0; i < count; i++) {
			//node.id = i;
			objectWriter.writeObject(node);
		}
		endDate = new Date();
		System.out.println("start: " + startDate);
		System.out.println("end: " + endDate);
		System.out.println("duration: " + (endDate.getTime() - startDate.getTime()) / 1000);
		
		dataStream.close();
	}
	
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		final int OBJECT_COUNT = 100000;
		Node node;
		
		node = new Node(1, new Date(), "The user", 2, 3);
		for (int i = 0; i < 10; i++) {
			node.addTag(new Tag("thekeyname" + i, "This is the key value for tag " + i + "."));
		}
		
		System.out.println("bdb");
		x(node, OBJECT_COUNT);
		System.out.println("custom");
		y(node, OBJECT_COUNT);
	}
}
