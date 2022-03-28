// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import crosby.binary.Osmformat;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Obtains metadata suitable for passing to {@link org.openstreetmap.osmosis.core.task.v0_6.Sink#initialize(Map)} from
 * a PBF {@link Osmformat.HeaderBlock}.
 */
public class HeaderMetadataReader implements Function<Osmformat.HeaderBlock, Map<String, Object>> {
    private static final List<String> SUPPORTED_FEATURES =
            Collections.unmodifiableList(Arrays.asList("OsmSchema-V0.6", "DenseNodes"));

    @Override
    public Map<String, Object> apply(Osmformat.HeaderBlock header) {
        // Check if there are any unsupported features in the file.
        // Build the list of active and unsupported features in the file.
        List<String> unsupportedFeatures = new ArrayList<>();
        for (String feature : header.getRequiredFeaturesList()) {
            if (!SUPPORTED_FEATURES.contains(feature)) {
                unsupportedFeatures.add(feature);
            }
        }

        // We can't continue if there are any unsupported features. We wait
        // until now so that we can display all unsupported features instead of
        // just the first one we encounter.
        if (unsupportedFeatures.size() > 0) {
            throw new OsmosisRuntimeException("PBF file contains unsupported features " + unsupportedFeatures);
        }

        Map<String, Object> osmosisMetadata = new HashMap<>();
        if (header.getOptionalFeaturesList().contains("LocationsOnWays")) {
            osmosisMetadata.put(WayNode.METADATA_KEY_LOCATION_INCLUDED, true);
        } else {
            osmosisMetadata.put(WayNode.METADATA_KEY_LOCATION_INCLUDED, false);
        }
        return osmosisMetadata;
    }
}
