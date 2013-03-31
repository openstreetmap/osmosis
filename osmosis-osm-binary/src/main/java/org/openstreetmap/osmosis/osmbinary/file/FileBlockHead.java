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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.google.protobuf.ByteString;

import org.openstreetmap.osmosis.osmbinary.Fileformat;

/**
 * Intermediate representation of the header of a fileblock when a set of
 * fileblocks is read as in a stream. The data in the fileblock must be either
 * skipped (where the returned value is a reference to the fileblock) or parsed.
 * 
 * @author crosby
 * 
 */
public class FileBlockHead extends FileBlockReference {
    protected FileBlockHead(String type, ByteString indexdata) {
        super(type, indexdata);
    }

    /**
     * Read the header. After reading the header, either the contents must be
     * skipped or read
     */
    static FileBlockHead readHead(InputStream input) throws IOException {
        DataInputStream datinput = new DataInputStream(input);
        int headersize = datinput.readInt();
        // System.out.format("Header size %d %x\n",headersize,headersize);
        if (headersize > MAX_HEADER_SIZE) {
          throw new FileFormatException("Unexpectedly long header "+MAX_HEADER_SIZE+ " bytes. Possibly corrupt file.");
        }
        
        byte buf[] = new byte[headersize];
        datinput.readFully(buf);
        // System.out.format("Read buffer for header of %d bytes\n",buf.length);
        Fileformat.BlobHeader header = Fileformat.BlobHeader
                .parseFrom(buf);
        FileBlockHead fileblock = new FileBlockHead(header.getType(), header
                .getIndexdata());

        fileblock.datasize = header.getDatasize();
        if (header.getDatasize() > MAX_BODY_SIZE) {
          throw new FileFormatException("Unexpectedly long body "+MAX_BODY_SIZE+ " bytes. Possibly corrupt file.");
        }
        
        fileblock.input = input;
        if (input instanceof FileInputStream)
            fileblock.data_offset = ((FileInputStream) input).getChannel()
                    .position();

        return fileblock;
    }

    /**
     * Assumes the stream is positioned over at the start of the data, skip over
     * it.
     * 
     * @throws IOException
     */
    void skipContents(InputStream input) throws IOException {
        if (input.skip(getDatasize()) != getDatasize())
            assert false : "SHORT READ";
    }

    /**
     * Assumes the stream is positioned over at the start of the data, read it
     * and return the complete FileBlock
     * 
     * @throws IOException
     */
    FileBlock readContents(InputStream input) throws IOException {
        DataInputStream datinput = new DataInputStream(input);
        byte buf[] = new byte[getDatasize()];
        datinput.readFully(buf);
        return parseData(buf);
    }
}
