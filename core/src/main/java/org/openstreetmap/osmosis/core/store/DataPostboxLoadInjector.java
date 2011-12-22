// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.Date;

import org.openstreetmap.osmosis.core.buffer.v0_6.EntityBuffer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.misc.v0_6.NullWriter;
import org.openstreetmap.osmosis.core.progress.v0_6.EntityProgressLogger;

/**
 * Very simple class for applying load to the DataPostbox class and measuring
 * performance.
 * 
 * @author Brett Henderson
 */
public final class DataPostboxLoadInjector implements Runnable {

	private EntityBuffer buffer;
	private EntityProgressLogger progressLogger;
	private NullWriter nullWriter;

	/**
	 * Launches the application.
	 * 
	 * @param args
	 *            The program arguments.
	 */
	public static void main(String[] args) {
		new DataPostboxLoadInjector().run();
	}

	
	private DataPostboxLoadInjector() {
		buffer = new EntityBuffer(10000);
		progressLogger = new EntityProgressLogger(5000, null);
		buffer.setSink(progressLogger);
		nullWriter = new NullWriter();
		progressLogger.setSink(nullWriter);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void run() {
		Thread t1;
		Thread t2;

		t1 = new Thread(new Writer(), "input");
		t2 = new Thread(buffer, "output");

		t1.start();
		t2.start();

		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class Writer implements Runnable {
		private Node node;
		private NodeContainer nodeContainer;

		public Writer() {
			node = new Node(new CommonEntityData(1, 2, new Date(), OsmUser.NONE, 3), 10, 10);
			nodeContainer = new NodeContainer(node);
		}

		public void run() {
			while (true) {
				buffer.process(nodeContainer);
			}
		}
	}
}
