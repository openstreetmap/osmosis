// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;


/**
 * Wraps a random access file adding buffered input stream capabilities. This
 * allows a file to be randomly accessed while providing performance
 * improvements over the non-buffered random access file implementation.
 * 
 * @author Brett Henderson
 */
public class BufferedRandomAccessFileInputStream extends InputStream {
	
	private static final int DEFAULT_BUFFER_COUNT = 4;
	private static final int DEFAULT_INITIAL_BUFFER_SIZE = 16;
	private static final int DEFAULT_MAXIMUM_BUFFER_SIZE = 4096;
	private static final float DEFAULT_BUFFER_INCREASE_FACTOR = 2;
	
	private RandomAccessFile randomFile;
	
	private int bufferCount;
	private List<BufferedReader> readerList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to be read.
	 * @throws FileNotFoundException
	 *             if the file cannot be opened.
	 */
	public BufferedRandomAccessFileInputStream(File file) throws FileNotFoundException {
		this(
				file,
				DEFAULT_BUFFER_COUNT,
				DEFAULT_INITIAL_BUFFER_SIZE,
				DEFAULT_MAXIMUM_BUFFER_SIZE,
				DEFAULT_BUFFER_INCREASE_FACTOR);
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param file
	 *            The file to be read.
	 * @param bufferCount
	 *            The number of buffers to use. Use of multiple buffers allows
	 *            some random seeks to occur without losing existing buffered
	 *            data that might be returned to.
	 * @param initialBufferSize
	 *            After a seek, this is the number of bytes that will be
	 *            initially read.
	 * @param maxBufferSize
	 *            This is the maximum number of bytes that will ever be read
	 *            from the underlying file at a time.
	 * @param bufferIncreaseFactor
	 *            During sequential reads, if the buffer is exhausted the next
	 *            read will be longer than the previous read according to this
	 *            factor. A value of 1 means the buffer never gets larger. The
	 *            buffer will never get larger than maxBufferSize.
	 * @throws FileNotFoundException
	 *             if the file cannot be opened.
	 */
	public BufferedRandomAccessFileInputStream(
			File file, int bufferCount, int initialBufferSize,
			int maxBufferSize, float bufferIncreaseFactor) throws FileNotFoundException {
		this.bufferCount = bufferCount;
		
		randomFile = new RandomAccessFile(file, "r");
		
		readerList = new LinkedList<BufferedReader>();
		for (int i = 0; i < bufferCount; i++) {
			readerList.add(new BufferedReader(randomFile, initialBufferSize, maxBufferSize, bufferIncreaseFactor));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		return readerList.get(0).read();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return readerList.get(0).read(b, off, len);
	}
	
	
	/**
	 * Seeks to the specified position in the file.
	 * 
	 * @param pos
	 *            The position within the file to seek to.
	 * @throws IOException
	 *             if an error occurs during seeking.
	 */
	public void seek(long pos) throws IOException {
		BufferedReader reader;
		
		reader = null;
		for (int i = 0; i < bufferCount; i++) {
			BufferedReader tmpReader;
			
			tmpReader = readerList.get(i);
			
			if (tmpReader.containsPosition(pos)) {
				reader = tmpReader;
				
				if (i > 0) {
					readerList.remove(i);
					readerList.add(0, reader);
				}
				
				break;
			}
		}
		
		if (reader == null) {
			reader = readerList.remove(bufferCount - 1);
			readerList.add(0, reader);
		}
		
		reader.seek(pos);
	}
	
	
	/**
	 * Returns the length of the data file.
	 * 
	 * @return The file length in bytes.
	 * @throws IOException
	 *             if an error occurs during the length operation.
	 */
	public long length() throws IOException {
		return randomFile.length();
	}
	
	
	/**
	 * Returns the current read position in the data file.
	 * 
	 * @return The current file offset in bytes.
	 * @throws IOException
	 *             if an error occurs during the position operation.
	 */
	public long position() throws IOException {
		return readerList.get(0).position();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		randomFile.close();
	}
	
	
	private static class BufferedReader {
		private RandomAccessFile randomFile;
		private int initialBufferSize;
		private int maxBufferSize;
		private float bufferIncreaseFactor;
		
		private byte[] buffer;
		private long bufferFilePosition;
		private int currentBufferSize;
		private int currentBufferByteCount;
		private int currentBufferOffset;
		
		
		/**
		 * Creates a new instance.
		 * 
		 * @param randomFile
		 *            The file to be read.
		 * @param initialBufferSize
		 *            After a seek, this is the number of bytes that will be
		 *            initially read.
		 * @param maxBufferSize
		 *            This is the maximum number of bytes that will ever be read
		 *            from the underlying file at a time.
		 * @param bufferIncreaseFactor
		 *            During sequential reads, if the buffer is exhausted the next
		 *            read will be longer than the previous read according to this
		 *            factor. A value of 1 means the buffer never gets larger. The
		 *            buffer will never get larger than maxBufferSize.
		 * @throws FileNotFoundException
		 *             if the file cannot be opened.
		 */
		public BufferedReader(
				RandomAccessFile randomFile, int initialBufferSize,
				int maxBufferSize, float bufferIncreaseFactor) {
			this.randomFile = randomFile;
			this.initialBufferSize = initialBufferSize;
			this.maxBufferSize = maxBufferSize;
			this.bufferIncreaseFactor = bufferIncreaseFactor;
			
			buffer = new byte[maxBufferSize];
			
			bufferFilePosition = 0;
			currentBufferSize = 0;
			currentBufferByteCount = 0;
			currentBufferOffset = 0;
		}
		
		
		/**
		 * Returns the current read position in the data file.
		 * 
		 * @return The current file offset in bytes.
		 * @throws IOException
		 *             if an error occurs during the position operation.
		 */
		public long position() throws IOException {
			return bufferFilePosition + currentBufferOffset;
		}


		/**
		 * Indicates if the specified position is contained within the current
		 * buffer. Note that the requested position may not be currently loaded
		 * in the buffer, it may be within maxBufferSize bytes of the buffer
		 * start position.
		 * 
		 * @param position
		 *            The requested file position.
		 * @return True if the position is within maxBufferSize bytes of the
		 *         buffer start.
		 */
		public boolean containsPosition(long position) {
			return (position >= bufferFilePosition) && (position < (bufferFilePosition + maxBufferSize));
		}
		
		
		/**
		 * Seeks to the specified position in the file.
		 * 
		 * @param pos
		 *            The position within the file to seek to.
		 * @throws IOException
		 *             if an error occurs during seeking.
		 */
		public void seek(long pos) throws IOException {
			long bufferBeginFileOffset;
			long bufferEndFileOffset;
			
			bufferBeginFileOffset = bufferFilePosition;
			bufferEndFileOffset = bufferFilePosition + currentBufferByteCount;
			
			// If the requested position is within the current buffer just move to
			// that position.
			if ((pos >= bufferBeginFileOffset) && pos <= (bufferEndFileOffset)) {
				// The request position is within the current buffer so just move to
				// that position.
				currentBufferOffset = (int) (pos - bufferBeginFileOffset);
			} else if ((pos < bufferBeginFileOffset) && (bufferBeginFileOffset - pos) <= maxBufferSize) {
				// The request position is within a max buffer size of the beginning
				// of the buffer so just move there without resetting the current
				// buffer size.
				randomFile.seek(pos);
				bufferFilePosition = pos;
				// Mark the current buffer as empty so that the next read will read
				// from disk.
				currentBufferByteCount = 0;
			} else if ((pos > bufferEndFileOffset) && (pos - bufferEndFileOffset) <= maxBufferSize) {
				// The request position is within a max buffer size of the end
				// of the buffer so just move there without resetting the current
				// buffer size.
				randomFile.seek(pos);
				bufferFilePosition = pos;
				// Mark the current buffer as empty so that the next read will read
				// from disk.
				currentBufferByteCount = 0;
			} else {
				// The request position is not close to the current position so move
				// there and reset the buffer completely.
				randomFile.seek(pos);
				bufferFilePosition = pos;
				currentBufferSize = 0;
				currentBufferByteCount = 0;
				currentBufferOffset = 0;
			}
		}
		
		
		/**
		 * Ensures data is available in the buffer.
		 * 
		 * @return True if data is available. False indicates that the end of stream
		 *         has been reached.
		 */
		private boolean populateBuffer() throws IOException {
			if (currentBufferOffset >= currentBufferByteCount) {
				// If another buffered reader has moved the file position, we need
				// to move it back.
				if (randomFile.getFilePointer() != (bufferFilePosition + currentBufferByteCount)) {
					randomFile.seek(bufferFilePosition + currentBufferByteCount);
				}
				
				// Update the current buffer file position to be at the
				// beginning of the next read location.
				bufferFilePosition += currentBufferByteCount;
				currentBufferOffset = 0;
				
				if (currentBufferSize == 0) {
					currentBufferSize = initialBufferSize;
				} else if (currentBufferSize < maxBufferSize) {
					currentBufferSize = (int) (currentBufferSize * bufferIncreaseFactor);
					if (currentBufferSize > maxBufferSize) {
						currentBufferSize = maxBufferSize;
					}
				}
				
				
				currentBufferByteCount = randomFile.read(buffer, 0, currentBufferSize);
				
				if (currentBufferByteCount < 0) {
					return false;
				}
			}
			
			return true;
		}
		
		
		/**
		 * Reads the next byte from the underlying stream.
		 * 
		 * @return The next byte.
		 * @throws IOException
		 *             if an error occurs.
		 */
		public int read() throws IOException {
			if (populateBuffer()) {
				return buffer[currentBufferOffset++] & 0xff;
			} else {
				return -1;
			}
		}


		/**
		 * Reads an array of bytes from the underlying stream.
		 * 
		 * @param b
		 *            The buffer to fill.
		 * @param off
		 *            The offset to fill the buffer from.
		 * @param len
		 *            The number of bytes to read.
		 * @return The number of bytes read.
		 * @throws IOException
		 *             if an error occurs.
		 */
		public int read(byte[] b, int off, int len) throws IOException {
			if (populateBuffer()) {
				int readLength;
				
				// Determine how many bytes to read from the current buffer.
				readLength = currentBufferByteCount - currentBufferOffset;
				if (readLength > len) {
					readLength = len;
				}
				
				// Copy the bytes into the output buffer and update the current buffer position.
				System.arraycopy(buffer, currentBufferOffset, b, off, readLength);
				currentBufferOffset += readLength;
				
				return readLength;
				
			} else {
				return -1;
			}
		}
	}
}
