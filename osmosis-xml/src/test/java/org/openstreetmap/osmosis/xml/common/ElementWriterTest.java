// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import junit.framework.Assert;


/**
 * Tests the element writer.
 */
public class ElementWriterTest {
	
	/**
	 * Tests the element writer.
	 * 
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	@Test
	public void testBasic() throws IOException {
		MyElementWriter elementWriter;
		StringWriter stringWriter;
		BufferedWriter bufferedWriter;
		
		stringWriter = new StringWriter();
		bufferedWriter = new BufferedWriter(stringWriter);
		
		elementWriter = new MyElementWriter();
		elementWriter.setWriter(bufferedWriter);
		
		elementWriter.buildContent();
		
		bufferedWriter.close();
		
		Assert.assertEquals(
				"Generated xml is incorrect.",
				"  <testElement myAttribute=\"ValueBeginValueEnd\"/>"
				+ System.getProperty("line.separator"),
				stringWriter.toString());
	}
	
	
	private static class MyElementWriter extends ElementWriter {
		public MyElementWriter() {
			super("testElement", 1);
		}
		
		
		public void buildContent() {
			beginOpenElement();
			addAttribute("myAttribute", "ValueBegin" + (char) 0x02 + "ValueEnd");
			endOpenElement(true);
		}
	}
}
