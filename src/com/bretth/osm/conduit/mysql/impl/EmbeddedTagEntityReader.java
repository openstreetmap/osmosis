package com.bretth.osm.conduit.mysql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bretth.osm.conduit.data.Tag;


public abstract class EmbeddedTagEntityReader<E> extends EntityReader<E> {
	
	public EmbeddedTagEntityReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	protected List<Tag> parseTags(String tags) {
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
