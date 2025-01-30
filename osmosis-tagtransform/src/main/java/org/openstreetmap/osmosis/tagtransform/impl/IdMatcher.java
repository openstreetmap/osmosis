// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import org.openstreetmap.osmosis.tagtransform.Match;
import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;

import java.util.*;


public class IdMatcher implements Matcher {

	private Set<Long> ids;
	private long matchHits;

	IdMatcher(Set<Long> ids) {
		this.ids = ids;
	}

	@Override
	public Collection<Match> match(long id, Map<String, String> tags, TTEntityType type, String uname, int uid) {
		if (!ids.contains(id))  {
			return null;
		}
		matchHits += 1;
		return Collections.singleton(NULL_MATCH);
	}


	@Override
	public void outputStats(StringBuilder output, String indent) {
		output.append(indent);
		output.append("Ids[");
		int i = 0;
		for (Long id: ids) {
			if (++i>1) {
				output.append(",");
			}
			if (i>5) {
				output.append("...");
				break;
			}
			output.append(id);
		}
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
