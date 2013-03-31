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

package org.openstreetmap.osmosis.osmbinary.file;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

enum CompressFlags {
    NONE, DEFLATE
}

public class BlockOutputStream {

    public BlockOutputStream(OutputStream output) {
        this.outwrite = new DataOutputStream(output);
        this.compression = CompressFlags.DEFLATE;
    }

    public void setCompress(CompressFlags flag) {
        compression = flag;
    }

    public void setCompress(String s) {
        if (s.equals("none"))
            compression = CompressFlags.NONE;
        else if (s.equals("deflate"))
            compression = CompressFlags.DEFLATE;
        else
            throw new Error("Unknown compression type: " + s);
    }

    /** Write a block with the stream's default compression flag */
    public void write(FileBlock block) throws IOException {
        this.write(block, compression);
    }

    /** Write a specific block with a specific compression flags */
    public void write(FileBlock block, CompressFlags compression)
            throws IOException {
        FileBlockPosition ref = block.writeTo(outwrite, compression);
        writtenblocks.add(ref);
    }

    public void flush() throws IOException {
        outwrite.flush();
    }

    public void close() throws IOException {
        outwrite.flush();
        outwrite.close();
    }

    OutputStream outwrite;
    List<FileBlockPosition> writtenblocks = new ArrayList<FileBlockPosition>();
    CompressFlags compression;
}
