// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import crosby.binary.Osmformat;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

import java.util.Optional;
import java.util.function.Function;

/**
 * Finds the first header in the stream.  It validates that a header occurs first.  This function has side effects in
 * that it moves the stream splitter iterator forward.
 */
public class HeaderSeeker implements Function<StreamSplitter, Osmformat.HeaderBlock> {
    private BlobToBlockMapper blobToBlockMapper = new BlobToBlockMapper();

    @Override
    public Osmformat.HeaderBlock apply(StreamSplitter streamSplitter) {
        if (!streamSplitter.hasNext()) {
            throw new OsmosisRuntimeException("PBF stream ended before a header could be found.");
        }
        RawBlob rawBlob = streamSplitter.next();
        PbfBlock block = blobToBlockMapper.apply(rawBlob);
        Optional<Osmformat.HeaderBlock> optionalHeaderBlock = block.getHeaderBlock();
        if (!optionalHeaderBlock.isPresent()) {
            throw new OsmosisRuntimeException("A non header block was encountered in the PBF file first");
        }
        return optionalHeaderBlock.get();
    }
}
