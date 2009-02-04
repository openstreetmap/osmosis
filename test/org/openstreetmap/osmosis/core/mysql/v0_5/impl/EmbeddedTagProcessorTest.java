// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;


/**
 * Tests the embedded tag processor used for storing node tags in a single field.
 */
public class EmbeddedTagProcessorTest {
	
	private static final String EMBEDDED_TAGS =
		"simpleKey1=simpleValue1;"
		+ "complexKey1=complexValue1a\\;complexValue1b;"
		+ "complexKey2=complex\\\\Value2";
	private static final List<Tag> OBJECT_TAGS = Arrays.asList(
			// A basic tag with no translation required.
			new Tag("simpleKey1", "simpleValue1"),
			// A tag with an embedded ';' character.
			new Tag("complexKey1", "complexValue1a;complexValue1b"),
			// A tag with an embedded '\' character.
			new Tag("complexKey2", "complex\\Value2"));
	
	
	/**
	 * Tests writing tags to a string.
	 */
	@Test
	public void testFormat() {
		String actualResult;
		
		actualResult = new EmbeddedTagProcessor().format(OBJECT_TAGS);
		
		Assert.assertEquals(
				"The result string is formatted incorrectly.",
				EMBEDDED_TAGS,
				actualResult);
	}
	
	
	/**
	 * Tests extracting tags from a string.
	 */
	@Test
	public void testParse() {
		List<Tag> actualResult;
		
		actualResult = new EmbeddedTagProcessor().parseTags(EMBEDDED_TAGS);
		
		// The tag class doesn't implement equals so we have to compare the tags directly.
		Assert.assertEquals(
				"The result list contains the wrong number of tags.", OBJECT_TAGS.size(), actualResult.size());
		for (int i = 0; i < actualResult.size(); i++) {
			Tag t1 = OBJECT_TAGS.get(0);
			Tag t2 = actualResult.get(0);
			
			Assert.assertEquals("The key for tag " + i + " is incorrect.", t1.getKey(), t2.getKey());
			Assert.assertEquals("The value for tag " + i + " is incorrect.", t1.getValue(), t2.getValue());
		}
	}
}
