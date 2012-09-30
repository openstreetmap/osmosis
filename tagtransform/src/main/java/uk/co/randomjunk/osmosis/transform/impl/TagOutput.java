// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.Match;
import uk.co.randomjunk.osmosis.transform.Output;

public class TagOutput implements Output {

	private MessageFormat keyFormat;
	private MessageFormat valueFormat;
	private String fromMatch;

	public TagOutput(String key, String value, String fromMatch) {
		keyFormat = new MessageFormat(santitise(key));
		valueFormat = new MessageFormat(santitise(value));
		this.fromMatch = (fromMatch == null || fromMatch.length() == 0) ? null : fromMatch;
	}

	private String santitise(String str) {
		if ( str == null || str.length() == 0 )
			return "{0}";
		return str;
	}

	@Override
	public void apply(Map<String, String> originalTags,
			Map<String, String> tags, Collection<Match> matches) {
		// if we have a fromMatch field we apply our format to
		// each and every matching match
		if ( fromMatch != null ) {
			for ( Match match : matches ) {
				String matchID = match.getMatchID();
				if ( matchID != null && matchID.equals(fromMatch) ) {
					// process key args
					String[] args = new String[match.getKeyGroupCount()];
					for ( int i = 0; i < args.length; i++ )
						args[i] = match.getKey(i);
					String key = keyFormat.format(args);
					
					// process value args
					args = new String[match.getValueGroupCount()];
					for ( int i = 0; i < args.length; i++ )
						args[i] = match.getValue(i);
					String value = valueFormat.format(args);
					
					// put the tag
					tags.put(key, value);
				}
			}
		} else {
			// simple case
			tags.put(keyFormat.format(null), valueFormat.format(null));
		}
	}

}
