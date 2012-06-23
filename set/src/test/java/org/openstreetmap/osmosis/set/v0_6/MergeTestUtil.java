// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.set.v0_6;

import org.junit.Ignore;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.testutil.v0_6.SinkEntityInspector;

/**
 * Utility methods for merge tests.
 * 
 * @author Igor Podolskiy
 */
@Ignore
public final class MergeTestUtil {

	private MergeTestUtil() {
	}
	
	/**
	 * Helper method to execute a simple merge of two sources.
	 * 
	 * @param merger the merge to use for the execution of the merge.
	 * @param source1 the first source to merge.
	 * @param source2 the second source to merge.
	 * @return the sink entity inspector containing the merged result.
	 * @throws Exception if something goes wrong.
	 */
	public static SinkEntityInspector merge(EntityMerger merger, 
			RunnableSource source1, RunnableSource source2) throws Exception {
		Thread t1 = new Thread(source1);
		Thread t2 = new Thread(source2);
		
		SinkEntityInspector inspector = new SinkEntityInspector();
		source1.setSink(merger.getSink(0));
		source2.setSink(merger.getSink(1));
		merger.setSink(inspector);
		
		Thread mThread = new Thread(merger);
		mThread.start();
		t1.start();
		t2.start();
		mThread.join();
		t1.join();
		t2.join();
		
		return inspector;
	}
}
