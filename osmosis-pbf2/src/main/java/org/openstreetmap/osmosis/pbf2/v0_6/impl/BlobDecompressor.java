// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import crosby.binary.Fileformat;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;

import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Obtains the raw uncompressed data from a {@link Fileformat.Blob}.
 */
public class BlobDecompressor implements Function<Fileformat.Blob, byte[]> {
    @Override
    public byte[] apply(Fileformat.Blob blob) {
        byte[] blobData;

        if (blob.hasRaw()) {
            blobData = blob.getRaw().toByteArray();
        } else if (blob.hasZlibData()) {
            Inflater inflater = new Inflater();
            inflater.setInput(blob.getZlibData().toByteArray());
            blobData = new byte[blob.getRawSize()];
            try {
                inflater.inflate(blobData);
            } catch (DataFormatException e) {
                throw new OsmosisRuntimeException("Unable to decompress PBF blob.", e);
            }
            if (!inflater.finished()) {
                throw new OsmosisRuntimeException("PBF blob contains incomplete compressed data.");
            }
        } else {
            throw new OsmosisRuntimeException("PBF blob uses unsupported compression, only raw or zlib may be used.");
        }

        return blobData;
    }
}
