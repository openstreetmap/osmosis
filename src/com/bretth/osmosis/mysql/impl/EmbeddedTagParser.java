package com.bretth.osmosis.mysql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bretth.osmosis.data.Tag;


/**
 * Provides functionality for extracting lists of tags from entities where
 * they're stored embedded in a single string field.
 * 
 * @author Brett Henderson
 */
public class EmbeddedTagParser {
	
	/**
	 * Parses the specified tag string and produces corresponding tag objects.
	 * 
	 * @param tags
	 *            The tag string.
	 * @return The tag objects.
	 */
	public List<Tag> parseTags(String tags) {
		StringTokenizer tokenizer;
		List<Tag> tagList;
		
		tagList = new ArrayList<Tag>();
		
		tokenizer = new StringTokenizer(tags, ";");
		
		while (tokenizer.hasMoreTokens()) {
			String token;
			String key;
			String value;
			int equalsIndex;
			
			token = tokenizer.nextToken();
			
			equalsIndex = token.indexOf("=");
			
			if (equalsIndex > 0) {
				key = token.substring(0, equalsIndex);
				value = token.substring(equalsIndex + 1, token.length());
			} else {
				key = token;
				value = "";
			}
			
			tagList.add(
				new Tag(key, value)
			);
		}
		
		return tagList;
	}
}
