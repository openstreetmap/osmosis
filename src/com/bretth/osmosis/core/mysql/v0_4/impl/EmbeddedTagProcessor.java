package com.bretth.osmosis.core.mysql.v0_4.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bretth.osmosis.core.domain.v0_4.Tag;


/**
 * Provides functionality for working with tags on entities where they're stored
 * embedded in a single string field.
 * 
 * @author Brett Henderson
 */
public class EmbeddedTagProcessor {
	
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
	
	
	/**
	 * Produces a string representing the complete list of tags.
	 * 
	 * @param tags
	 *            The list of tag objects.
	 * @return A single string representing all tags.
	 */
	public String format(List<Tag> tags) {
		StringBuilder tagBuffer;
		
		tagBuffer = new StringBuilder();
		for (Tag tag : tags) {
			if (tagBuffer.length() > 0) {
				tagBuffer.append(';');
			}
			
			tagBuffer.append(tag.getKey()).append("=").append(tag.getValue());
		}
		
		return tagBuffer.toString();
	}
}
