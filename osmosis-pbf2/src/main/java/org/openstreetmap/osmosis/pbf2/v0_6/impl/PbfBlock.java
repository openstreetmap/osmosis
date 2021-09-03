// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.pbf2.v0_6.impl;

import crosby.binary.Osmformat;

import java.util.Optional;

/**
 * Contains the results of parsing a {@link crosby.binary.Fileformat.Blob}.
 */
public class PbfBlock {
    private Optional<Osmformat.HeaderBlock> headerBlock;
    private Optional<Osmformat.PrimitiveBlock> primitiveBlock;

    /**
     * Creates a new instance with no data.
     */
    public PbfBlock() {
        this.headerBlock = Optional.empty();
        this.primitiveBlock = Optional.empty();
    }

    /**
     * Creates a new instance with a header.
     * @param headerBlock The header block.
     */
    public PbfBlock(Osmformat.HeaderBlock headerBlock) {
        this.headerBlock = Optional.of(headerBlock);
        this.primitiveBlock = Optional.empty();
    }

    /**
     * Creates a new instance with primitives.
     * @param primitiveBlock The primitive block.
     */
    public PbfBlock(Osmformat.PrimitiveBlock primitiveBlock) {
        this.headerBlock = Optional.empty();
        this.primitiveBlock = Optional.of(primitiveBlock);
    }

    /**
     * Gets the header block if available.
     * @return The optional header block.
     */
    public Optional<Osmformat.HeaderBlock> getHeaderBlock() {
        return headerBlock;
    }

    /**
     * Gets the primitive block if available.
     * @return The primitive block.
     */
    public Optional<Osmformat.PrimitiveBlock> getPrimitiveBlock() {
        return primitiveBlock;
    }
}
