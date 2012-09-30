// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform;

import java.util.Collection;
import java.util.Map;


public interface Output {

	void apply(Map<String, String> originalTags, Map<String, String> tags, Collection<Match> matches);

}
