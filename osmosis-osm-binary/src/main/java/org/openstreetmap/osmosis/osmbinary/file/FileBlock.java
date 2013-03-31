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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;

import com.google.protobuf.ByteString;

import org.openstreetmap.osmosis.osmbinary.Fileformat;
import org.openstreetmap.osmosis.osmbinary.Fileformat.BlobHeader;

/** A full fileblock object contains both the metadata and data of a fileblock */
public class FileBlock extends FileBlockBase {
    /** Contains the contents of a block for use or further processing */
    ByteString data; // serialized Format.Blob

    /** Don't be noisy unless the warning occurs somewhat often */
    static int warncount = 0;

    private FileBlock(String type, ByteString blob, ByteString indexdata) {
        super(type, indexdata);
        this.data = blob;
    }

    public static FileBlock newInstance(String type, ByteString blob,
            ByteString indexdata) {
      if (blob != null && blob.size() > MAX_BODY_SIZE/2) {
        System.err.println("Warning: Fileblock has body size too large and may be considered corrupt");
        if (blob != null && blob.size() > MAX_BODY_SIZE-1024*1024) {
          throw new Error("This file has too many entities in a block. Parsers will reject it.");
        }
      }
      if (indexdata != null && indexdata.size() > MAX_HEADER_SIZE/2) {
        System.err.println("Warning: Fileblock has indexdata too large and may be considered corrupt");
        if (indexdata != null && indexdata.size() > MAX_HEADER_SIZE-512) {
          throw new Error("This file header is too large. Parsers will reject it.");
        }
      }
      return new FileBlock(type, blob, indexdata);
    }

     protected void deflateInto(org.openstreetmap.osmosis.osmbinary.Fileformat.Blob.Builder blobbuilder) {
        int size = data.size();
        Deflater deflater = new Deflater();
        deflater.setInput(data.toByteArray());
        deflater.finish();
        byte out[] = new byte[size];
        deflater.deflate(out);
        
        if (!deflater.finished()) {
            // Buffer wasn't long enough. Be noisy.
          ++warncount;
          if (warncount > 10 && warncount%100 == 0)
               System.out.println("Compressed buffers are too short, causing extra copy");
            out = Arrays.copyOf(out, size + size / 64 + 16);
            deflater.deflate(out, deflater.getTotalOut(), out.length
                    - deflater.getTotalOut());
            if (!deflater.finished()) {
              throw new Error("Internal error in compressor");
            }
        }
        ByteString compressed = ByteString.copyFrom(out, 0, deflater
                .getTotalOut());
        blobbuilder.setZlibData(compressed);
        deflater.end();
    }

    public FileBlockPosition writeTo(OutputStream outwrite, CompressFlags flags)
            throws IOException {
        BlobHeader.Builder builder = Fileformat.BlobHeader
                .newBuilder();
        if (indexdata != null)
            builder.setIndexdata(indexdata);
        builder.setType(type);

        Fileformat.Blob.Builder blobbuilder = Fileformat.Blob.newBuilder();
        if (flags == CompressFlags.NONE) {
            blobbuilder.setRaw(data);
            blobbuilder.setRawSize(data.size());
        } else {
            blobbuilder.setRawSize(data.size());
            if (flags == CompressFlags.DEFLATE)
                deflateInto(blobbuilder);
            else
                throw new Error("Compression flag not understood");
        }
        Fileformat.Blob blob = blobbuilder.build();

        builder.setDatasize(blob.getSerializedSize());
        Fileformat.BlobHeader message = builder.build();
        int size = message.getSerializedSize();

        // System.out.format("Outputed header size %d bytes, header of %d bytes, and blob of %d bytes\n",
        // size,message.getSerializedSize(),blob.getSerializedSize());
        (new DataOutputStream(outwrite)).writeInt(size);
        message.writeTo(outwrite);
        long offset = -1;

        if (outwrite instanceof FileOutputStream)
            offset = ((FileOutputStream) outwrite).getChannel().position();

        blob.writeTo(outwrite);
        return FileBlockPosition.newInstance(this, offset, size);
    }

    /** Reads or skips a fileblock. */
    static void process(InputStream input, BlockReaderAdapter callback)
            throws IOException {
        FileBlockHead fileblock = FileBlockHead.readHead(input);
        if (callback.skipBlock(fileblock)) {
            // System.out.format("Attempt to skip %d bytes\n",header.getDatasize());
            fileblock.skipContents(input);
        } else {
            callback.handleBlock(fileblock.readContents(input));
        }
    }

    public ByteString getData() {
        return data;
    }
}
