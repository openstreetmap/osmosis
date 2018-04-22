// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.osmbinary.Fileformat;
import org.openstreetmap.osmosis.osmbinary.Osmformat;

import java.util.function.Function;

/**
 * Parses the raw blob data from a PBF stream into a strongly typed block.
 */
public class BlobToBlockMapper implements Function<RawBlob, PbfBlock> {
    private static final String HEADER_TYPE = "OSMHeader";
    private static final String PRIMITIVE_TYPE = "OSMData";

    private BlobDecompressor decompressor = new BlobDecompressor();

    @Override
    public PbfBlock apply(RawBlob rawBlob) {
        Fileformat.Blob pbfBlob = parseBlob(rawBlob.getData());
        byte[] data = decompressor.apply(pbfBlob);

        if (HEADER_TYPE.equals(rawBlob.getType())) {
            return new PbfBlock(parseHeaderBlock(data));
        } else if (PRIMITIVE_TYPE.equals((rawBlob.getType()))) {
            return new PbfBlock(parsePrimitiveBlock(data));
        } else {
            return new PbfBlock();
        }
    }

    private Fileformat.Blob parseBlob(byte[] data) {
        try {
            return Fileformat.Blob.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new OsmosisRuntimeException("Unable to parse PBF blob", e);
        }
    }

    private Osmformat.HeaderBlock parseHeaderBlock(byte[] data) {
        try {
            return Osmformat.HeaderBlock.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new OsmosisRuntimeException("Unable to parse PBF header block", e);
        }
    }

    private Osmformat.PrimitiveBlock parsePrimitiveBlock(byte[] data) {
        try {
            return Osmformat.PrimitiveBlock.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new OsmosisRuntimeException("Unable to parse PBF primitive block", e);
        }
    }
}
