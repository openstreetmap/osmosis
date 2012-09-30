// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;


public class NoTagMatcher implements Matcher {

	private Pattern keyPattern;
	private Pattern valuePattern;
	private long matchHits;


	public NoTagMatcher(String keyPattern, String valuePattern) {
		this.keyPattern = Pattern.compile(keyPattern);
		this.valuePattern = Pattern.compile(valuePattern);
	}


	@Override
	public Collection<Match> match(Map<String, String> tags, TTEntityType type, String uname, int uid) {
		// loop through the tags to find matches
		for (Entry<String, String> tag : tags.entrySet()) {
			java.util.regex.Matcher keyMatch = keyPattern.matcher(tag.getKey());
			java.util.regex.Matcher valueMatch = valuePattern.matcher(tag.getValue());
			if (keyMatch.matches() && valueMatch.matches()) {
				return null;
			}
		}

		matchHits += 1;
		return Collections.singleton(NULL_MATCH);
	}


	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("NoTag[");
		output.append(keyPattern.pattern());
		output.append(",");
		output.append(valuePattern.pattern());
		output.append("]: ");
		output.append(matchHits);
		output.append('\n');
	}

	private static final Match NULL_MATCH = new Match() {
		@Override
		public int getValueGroupCount() {
			return 0;
		}


		@Override
		public String getValue(int group) {
			return null;
		}


		@Override
		public String getMatchID() {
			return null;
		}


		@Override
		public int getKeyGroupCount() {
			return 0;
		}


		@Override
		public String getKey(int group) {
			return null;
		}
	};
}
