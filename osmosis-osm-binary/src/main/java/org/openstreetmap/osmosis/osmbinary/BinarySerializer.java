/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as 
   published by the Free Software Foundation, either version 3 of the 
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.openstreetmap.osmosis.osmbinary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.osmbinary.Osmformat.PrimitiveGroup;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import org.openstreetmap.osmosis.osmbinary.file.FileBlock;

/**
 * Generic serializer common code
 * 
 * Serialize a set of blobs and process them. Subclasses implement handlers for
 * different API's (osmosis, mkgmap, splitter, etc.)
 * 
 * All data is converted into PrimGroupWriterInterface objects, which are then
 * ordered to process their data at the appropriate time.
 * */

public class BinarySerializer {

    /**
     * Interface used to write a group of primitives. One of these for each
     * group type (Node, Way, Relation, DenseNode, Changeset)
     */
    protected interface PrimGroupWriterInterface {
        /** This callback is invoked on each group that is going into the fileblock in order to give it a chance to 
         * add to the stringtable pool of strings. */
        public void addStringsToStringtable();

        /**
         * This callback is invoked to request that the primgroup serialize itself into the given protocol buffer object.
         */
        public Osmformat.PrimitiveGroup serialize();
    }

    /** Set the granularity (precision of lat/lon, measured in unites of nanodegrees. */
    public void configGranularity(int granularity) {
        this.granularity = granularity;
    }

    /** Set whether metadata is to be omitted */
    public void configOmit(boolean omit_metadata) {
        this.omit_metadata = omit_metadata;
    }

    /** Configure the maximum number of entities in a batch */
    public void configBatchLimit(int batch_limit) {
        this.batch_limit = batch_limit;
    }

    // Paramaters affecting the output size.
    protected final int MIN_DENSE = 10;
    protected int batch_limit = 4000;

    // Parmaters affecting the output.

    protected int granularity = 100;
    protected int date_granularity = 1000;
    protected boolean omit_metadata = false;

    /** How many primitives have been seen in this batch */
    protected int batch_size = 0;
    protected int total_entities = 0;
    private StringTable stringtable = new StringTable();
    protected List<PrimGroupWriterInterface> groups = new ArrayList<PrimGroupWriterInterface>();
    protected BlockOutputStream output;

    public BinarySerializer(BlockOutputStream output) {
        this.output = output;
    }

    public StringTable getStringTable() {
        return stringtable;
    }

    public void flush() throws IOException {
        processBatch();
        output.flush();
    }

    public void close() throws IOException {
        flush();
        output.close();
    }

    long debug_bytes = 0;

    public void processBatch() {
        // System.out.format("Batch of %d groups: ",groups.size());
        if (groups.size() == 0)
            return;
        Osmformat.PrimitiveBlock.Builder primblock = Osmformat.PrimitiveBlock
                .newBuilder();
        stringtable.clear();
        // Preprocessing: Figure out the stringtable.
        for (PrimGroupWriterInterface i : groups)
            i.addStringsToStringtable();

        stringtable.finish();
        // Now, start serializing.
        for (PrimGroupWriterInterface i : groups) {
         PrimitiveGroup group = i.serialize();
         if (group != null)
           primblock.addPrimitivegroup(group);
        }
        primblock.setStringtable(stringtable.serialize());
        primblock.setGranularity(this.granularity);
        primblock.setDateGranularity(this.date_granularity);

        // Only generate data with offset (0,0)
        // 
        Osmformat.PrimitiveBlock message = primblock.build();

        // System.out.println(message);
        debug_bytes += message.getSerializedSize();
        // if (message.getSerializedSize() > 1000000)
        // System.out.println(message);

        try {
            output.write(FileBlock.newInstance("OSMData", message
                    .toByteString(), null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new Error(e);
        } finally {
            batch_size = 0;
            groups.clear();
        }
        // System.out.format("\n");
    }

    /** Convert from a degrees represented as a double into the serialized offset in nanodegrees.. */
    public long mapRawDegrees(double degrees) {
        return (long) ((degrees / .000000001));
    }

    /** Convert from a degrees represented as a double into the serialized offset. */
    public int mapDegrees(double degrees) {
        return (int) ((degrees / .0000001) / (granularity / 100));
    }
}
