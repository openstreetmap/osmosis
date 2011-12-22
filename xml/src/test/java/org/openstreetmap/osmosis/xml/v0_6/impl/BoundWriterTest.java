// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

/**
 * Tests the Bound class implementation.
 */
public class BoundWriterTest {

	private StringWriter testWriter;
	private BufferedWriter testBufferedWriter;


	/**
	 * Performs pre-test activities.
	 */
	@Before
	public void setUp() {
		testWriter = new StringWriter();
		testBufferedWriter = new BufferedWriter(testWriter);
	}


	/**
	 * Performs post-test activities.
	 * 
	 * @throws IOException
	 *             if stream cleanup fails.
	 */
	@After
	public void tearDown() throws IOException {
		testBufferedWriter.close();
		testWriter.close();
	}


	/**
	 * Test writing out a normal Bound element. 
	 */
	@Test
	public final void testProcess1() {
		BoundWriter bw = new BoundWriter("bound", 2, true);
		bw.setWriter(testBufferedWriter);
		bw.process(new Bound(20.123456, -21.987654, 22.555555, -23.234567, "originstring"));
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		// If this test fails, it could be because the regex has broken. There are a number of
		// variations which are valid XML which this regex won't catch. It might need any number of
		// \\s* to account for variable whitespace.
		String regexMatch = "^\\s*<bound\\s*"
		        + "box=['\"]-23.23457,-21.98765,22.55556,20.12346['\"]\\s*"
		        + "origin=['\"]originstring['\"]/>\\s*$";
		assertTrue(testWriter.toString().matches(regexMatch));
	}

	
	/**
	 * Test non-writing of a Bound element with an empty origin string. 
	 */
	@Test
	public final void testProcess2() {
		BoundWriter bw = new BoundWriter("bound", 2, true);
		bw.setWriter(testBufferedWriter);
		bw.process(new Bound(20.123456, -21.987654, 22.555555, -23.234567, ""));
		try {
			testBufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(testWriter.toString().equals("")); // not written; empty string
	}
}
