// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.osmosis.core.domain.v0_5.Tag;


/**
 * Provides functionality for working with tags on entities where they're stored
 * embedded in a single string field.
 * 
 * @author Brett Henderson
 */
public class EmbeddedTagProcessor {
	
	private Pattern slashEscapePattern;
	private Pattern semiColonEscapePattern;
	private Pattern slashUnescapePattern;
	private String slashEscapeReplacement;
	private String semiColonEscapeReplacement;
	private String slashUnescapeReplacement;
	
	
	/**
	 * Creates a new instance.
	 */
	public EmbeddedTagProcessor() {
		slashEscapePattern = Pattern.compile("\\", Pattern.LITERAL);
		semiColonEscapePattern = Pattern.compile(";");
		slashUnescapePattern = Pattern.compile("\\\\", Pattern.LITERAL);
		slashEscapeReplacement = Matcher.quoteReplacement("\\\\");
		semiColonEscapeReplacement = Matcher.quoteReplacement("\\;");
		slashUnescapeReplacement = Matcher.quoteReplacement("\\");
	}
	
	
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
		StringBuilder token;
		
		tagList = new ArrayList<Tag>();
		
		tokenizer = new StringTokenizer(tags, ";");
		token = new StringBuilder();
		
		while (tokenizer.hasMoreTokens()) {
			String key;
			String value;
			int equalsIndex;
			String unescapedToken;
			
			// Append the next token to the token buffer.
			token.append(tokenizer.nextToken());
			
			// If the current token ends with '\' and there are more tokens then
			// the ';' wasn't a token separator, it was escaped.
			if (token.charAt(token.length() - 1) == '\\' && tokenizer.hasMoreTokens()) {
				// Replace the '\' that was read with the ';' which was thought
				// to be a separator by the tokenizer.
				token.setCharAt(token.length() - 1, ';');
				
				// Begin at the start of the loop again.
				continue;
			}
			
			// Replace the escaped '\' sequences (ie. "\\" with a single '\').
			unescapedToken = slashUnescapePattern.matcher(token.toString()).replaceAll(slashUnescapeReplacement);
			
			// Find the location of the '=' character separating the tag key and value.
			equalsIndex = unescapedToken.indexOf("=");
			
			// If the '=' is not found, the entire token is the key and the value is empty.
			if (equalsIndex > 0) {
				key = unescapedToken.substring(0, equalsIndex);
				value = unescapedToken.substring(equalsIndex + 1, unescapedToken.length());
			} else {
				key = unescapedToken;
				value = "";
			}
			
			// Add the tag to the list.
			tagList.add(
				new Tag(key, value)
			);
			// Reset the tag data buffer.
			token.setLength(0);
		}
		
		return tagList;
	}
	
	
	private String escapeValue(String value) {
		String escapedValue;
		
		escapedValue = value;
		
		// Replace "\" with "\\".
		escapedValue = slashEscapePattern.matcher(escapedValue).replaceAll(slashEscapeReplacement);
		// Replace ";" with "\;".
		escapedValue = semiColonEscapePattern.matcher(escapedValue).replaceAll(semiColonEscapeReplacement);
		
		return escapedValue;
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
			
			tagBuffer.append(tag.getKey()).append("=").append(escapeValue(tag.getValue()));
		}
		
		return tagBuffer.toString();
	}
}
