package com.bretth.osm.conduit.mysql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.bretth.osm.conduit.data.Tag;


/**
 * Extends basic entity reader functionality with features for reading tag
 * objects embedded within a single field in the same database record.
 * 
 * @author Brett Henderson
 * 
 * @param <EntityType>
 *            The type of entity to retrieved.
 */
public abstract class EmbeddedTagEntityReader<EntityType> extends EntityReader<EntityType> {
	
	/**
	 * Creates a new instance.
	 * 
	 * @param host
	 *            The server hosting the database.
	 * @param database
	 *            The database instance.
	 * @param user
	 *            The user name for authentication.
	 * @param password
	 *            The password for authentication.
	 */
	public EmbeddedTagEntityReader(String host, String database, String user, String password) {
		super(host, database, user, password);
	}
	
	
	/**
	 * Parses the specified tag string and produces corresponding tag objects.
	 * 
	 * @param tags
	 *            The tag string.
	 * @return The tag objects.
	 */
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
