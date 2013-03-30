// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.testutil.v0_6;

import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;

/**
 * Utility methods for running complicated tasks like MultiSinks.
 * 
 * @author Igor Podolskiy
 */
public final class RunTaskUtilities {

	private RunTaskUtilities() {
	}
	
	/**
	 * Helper method to execute a two-sink entity source.
	 * 
	 * @param multiSink the multi-sink source to run.
	 * @param source1 the first source to feed to the sink.
	 * @param source2 the second source to feed to the sink.
	 * @return the sink entity inspector containing the result.
	 * @throws Exception if something goes wrong.
	 */
	public static SinkEntityInspector run(MultiSinkRunnableSource multiSink, 
			RunnableSource source1, RunnableSource source2) throws Exception {
		return run(multiSink, source1, source2, null);
	}
	
	/**
	 * Helper method to execute a two-sink entity source.
	 * 
	 * @param multiSink the multi-sink source to run.
	 * @param source1 the first source to feed to the sink.
	 * @param source2 the second source to feed to the sink.
	 * @param exceptionHandler the exception handler to attach to threads.
	 * @return the sink entity inspector containing the result.
	 * @throws Exception if something goes wrong.
	 */
	public static SinkEntityInspector run(MultiSinkRunnableSource multiSink, 
			RunnableSource source1, RunnableSource source2,
			Thread.UncaughtExceptionHandler exceptionHandler) throws Exception {
		
		SinkEntityInspector inspector = new SinkEntityInspector();
		source1.setSink(multiSink.getSink(0));
		source2.setSink(multiSink.getSink(1));
		multiSink.setSink(inspector);
		
		runCore(multiSink, source1, source2, exceptionHandler);
		
		return inspector;
	}

	/**
	 * Helper method to execute a two-sink change source.
	 * 
	 * @param multiSink the twp-sink change source to run.
	 * @param source1 the first source to feed to the sink.
	 * @param source2 the second source to feed to the sink.
	 * @return the sink change inspector containing the result.
	 * @throws Exception if something goes wrong.
	 */
	public static SinkChangeInspector run(MultiSinkRunnableChangeSource multiSink, 
			RunnableSource source1, RunnableSource source2) throws Exception {
		return run(multiSink, source1, source2, null);
	}

	
	/**
	 * Helper method to execute a two-sink change source.
	 * 
	 * @param multiSink the twp-sink change source to run.
	 * @param source1 the first source to feed to the sink.
	 * @param source2 the second source to feed to the sink.
	 * @param exceptionHandler the exception handler to attach to threads.
	 * @return the sink change inspector containing the result.
	 * @throws Exception if something goes wrong.
	 */
	public static SinkChangeInspector run(MultiSinkRunnableChangeSource multiSink, 
			RunnableSource source1, RunnableSource source2,
			Thread.UncaughtExceptionHandler exceptionHandler) throws Exception {
		SinkChangeInspector inspector = new SinkChangeInspector();
		
		source1.setSink(multiSink.getSink(0));
		source2.setSink(multiSink.getSink(1));
		multiSink.setChangeSink(inspector);
		
		runCore(multiSink, source1, source2, exceptionHandler);
		
		return inspector;
	}
	
	private static void runCore(Runnable multiSink,
			Runnable source1, Runnable source2,
			Thread.UncaughtExceptionHandler exceptionHandler)
			throws InterruptedException {

		Thread sourceThread1 = new Thread(source1);
		Thread sourceThread2 = new Thread(source2);
		Thread sinkThread = new Thread(multiSink);
		
		if (exceptionHandler != null) {
			sourceThread1.setUncaughtExceptionHandler(exceptionHandler);
			sourceThread2.setUncaughtExceptionHandler(exceptionHandler);
			sinkThread.setUncaughtExceptionHandler(exceptionHandler);
		}

		sinkThread.start();
		sourceThread1.start();
		sourceThread2.start();
		
		sinkThread.join();
		sourceThread1.join();
		sourceThread2.join();
	}
}
