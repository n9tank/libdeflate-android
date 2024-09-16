/*
 * Copyright 2024 Andrew Steinborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.steinborn.libdeflate;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;

/**
 * Represents a {@code libdeflate} decompressor. This class contains compression methods for byte
 * arrays and NIO ByteBuffers.
 *
 * <p><strong>Thread-safety</strong>: libdeflate decompressors are not thread-safe, however using
 * multiple decompressors per thread is permissible.
 */
public class LibdeflateDecompressor implements Closeable, AutoCloseable {
 static {
  Libdeflate.ensureAvailable();
  initIDs();
 }

 private final long ctx;
 private int availInBytes;
 private final int mode;
 /** Creates a new libdeflate decompressor. */
 public LibdeflateDecompressor(int mode) {
  this.ctx = allocate();
  this.mode = mode;
 }
 public int readStreamBytes() {
  int bytes = availInBytes;
  if (bytes < 0)throw new IllegalStateException("No byte array decompression done yet!");
  availInBytes = -1;
  return bytes;
 }
 /**
  * Decompresses the given {@code in} array into the {@code out} array. This method assumes that
  * the length of {@code out} is the size of the uncompressed output. If this is not true, use
  * {@link #decompressUnknownSize(byte[], byte[], CompressionType)} instead.
  *
  * @param in the source array with compressed
  * @param out the destination which will hold decompressed data
  * @param type the compression container to use
  * @throws DataFormatException if the provided data was corrupt, or the data decompressed
  *     successfully but it is less than the size of the output buffer
  */
 public int decompress(byte[] in, byte[] out) throws DataFormatException {
  return decompress(in, out, out.length);
 }

 /**
  * Decompresses the given {@code in} array into the {@code out} array. This method assumes the
  * uncompressed size of the data is known.
  *
  * @param in the source array with compressed
  * @param out the destination which will hold decompressed data
  * @param type the compression container to use
  * @param uncompressedSize the known size of the data
  * @throws DataFormatException if the provided data was corrupt, or the data decompressed
  *     successfully but not to {@code uncompressedSize}
  */
 public int decompress(byte[] in, byte[] out,  int uncompressedSize)
 throws DataFormatException {
  return decompressBothHeap(
   in, 0, in.length, out, 0, out.length, mode, uncompressedSize);
 }

 /**
  * Decompresses the given {@code in} array into the {@code out} array. This method assumes the
  * uncompressed size of the data is known.
  *
  * @param in the source array with compressed data
  * @param inOff the offset into the source array
  * @param inLen the length into the source array from the offset
  * @param out the destination which will hold decompressed data
  * @param outOff the offset into the source array
  * @param type the compression container to use
  * @param uncompressedSize the known size of the data, which is also treated as the length into
  *     the output array from {@code outOff}
  * @throws DataFormatException if the provided data was corrupt, or the data decompressed
  *     successfully but not to {@code uncompressedSize}
  */
 public int decompress(
  byte[] in,
  int inOff,
  int inLen,
  byte[] out,
  int outOff,
  int outLen,
  int uncompressedSize)
 throws DataFormatException {
  return decompressBothHeap(
   in, inOff, inLen, out, outOff, outLen, mode, uncompressedSize);
 }

 /**
  * Decompresses the given {@code in} array into the {@code out} array. This method assumes the
  * uncompressed size of the data is unknown. Note that using libdeflate's decompressor when the
  * uncompressed size of the data is not known is not recommended, because libdeflate does not have
  * a streaming API. If you require a streaming API, you are better served by the {@code Deflater}
  * and {@code Inflater} classes in {@code java.util.zip}.
  *
  * @param in the source array with compressed
  * @param out the destination which will hold decompressed data
  * @param type the compression container to use
  * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the
  *     given output buffer was too small
  * @throws DataFormatException if the provided data was corrupt
  */
 public int decompressUnknownSize(byte[] in, byte[] out)
 throws DataFormatException {
  return decompressBothHeap(in, 0, in.length, out, 0, out.length, mode, -1);
 }

 /**
  * Decompresses the given {@code in} array into the {@code out} array. This method assumes the
  * uncompressed size of the data is unknown. Note that using libdeflate's decompressor when the
  * uncompressed size of the data is not known is not recommended, because libdeflate does not have
  * a streaming API. If you require a streaming API, you are better served by the {@code Deflater}
  * and {@code Inflater} classes in {@code java.util.zip}.
  *
  * @param in the source array with compressed
  * @param inOff the offset into the source array
  * @param inLen the length into the source array from the offset
  * @param out the destination which will hold decompressed data
  * @param outOff the offset into the source array
  * @param outLen the length into the source array from {@code outOff}
  * @param type the compression container to use
  * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the
  *     given output buffer was too small
  * @throws DataFormatException if the provided data was corrupt
  */
 public int decompressUnknownSize(
  byte[] in, int inOff, int inLen, byte[] out, int outOff, int outLen)
 throws DataFormatException {
  return decompressBothHeap(in, inOff, inLen, out, outOff, outLen, mode, -1);
 }

 private int decompress0(
  ByteBuffer in, ByteBuffer out,  int uncompressedSize)
 throws DataFormatException {
  int nativeType = mode;

  int inAvail = in.remaining();
  int outAvail = out.remaining();

  // Either ByteBuffer could be direct or heap.
  int outRealSize;
  if (in.isDirect()) {
   if (out.isDirect()) {
    outRealSize =
     decompressBothDirect(
     in,
     in.position(),
     inAvail,
     out,
     out.position(),
     outAvail,
     nativeType,
     uncompressedSize);
   } else {
    outRealSize =
     decompressOnlySourceDirect(
     in,
     in.position(),
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     outAvail,
     nativeType,
     uncompressedSize);
   }
  } else {
   int inPos = byteBufferArrayPosition(in);
   if (out.isDirect()) {
    outRealSize =
     decompressOnlyDestinationDirect(
     in.array(),
     inPos,
     inAvail,
     out,
     out.position(),
     outAvail,
     nativeType,
     uncompressedSize);
   } else {
    outRealSize =
     decompressBothHeap(
     in.array(),
     inPos,
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     outAvail,
     nativeType,
     uncompressedSize);
   }
  }
  if (uncompressedSize != -1)outRealSize = uncompressedSize;
  out.position(out.position() + outRealSize);
  in.position(in.position() + this.readStreamBytes());
  return outRealSize;
 }

 /**
  * Decompresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the
  * decompression operation completes, the {@code position} of the output buffer will be
  * incremented by the number of bytes produced, and the input {@code position} will be incremented
  * by the number of bytes read. This function assumes the size of the uncompressed data is the
  * amount of bytes remaining in the buffer (the limit minus its position).
  *
  * @param in the source byte buffer to decompress
  * @param out the destination which will hold decompressed data
  * @param type the compression container in use
  * @throws DataFormatException if the provided data was corrupt, or the data decompresses to an
  *     invalid size
  */
 public int decompress(ByteBuffer in, ByteBuffer out)
 throws DataFormatException {
  return decompress0(in, out, out.remaining());
 }

 /**
  * Decompresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the
  * decompression operation completes, the {@code position} of the output buffer will be
  * incremented by the number of bytes produced, and the input {@code position} will be incremented
  * by the number of bytes read.
  *
  * @param in the source byte buffer to decompress
  * @param out the destination which will hold decompressed data
  * @param type the compression container in use
  * @throws DataFormatException if the provided data was corrupt, or the data decompresses to an
  *     invalid size
  */
 public int decompress(ByteBuffer in, ByteBuffer out,  int uncompressedSize)
 throws DataFormatException {
  return decompress0(in, out, uncompressedSize);
 }

 /**
  * Decompresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the
  * decompression operation completes, the {@code position} of the output buffer will be
  * incremented by the number of bytes produced, and the input {@code position} will be incremented
  * by the number of bytes read.
  *
  * <p>Note that using libdeflate's decompressor when the uncompressed size of the data is not
  * known is not recommended, because libdeflate does not have a streaming API. If you require a
  * streaming API, you are better served by the {@code Deflater} and {@code Inflater} classes in
  * {@code java.util.zip}.
  *
  * @param in the source byte buffer to decompress
  * @param out the destination which will hold decompressed data
  * @param type the compression container in use
  * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the
  *     given output buffer was too small
  * @throws DataFormatException if the provided data was corrupt, or the data decompresses to an
  *     invalid size
  */
 public int decompressUnknownSize(ByteBuffer in, ByteBuffer out)
 throws DataFormatException {
  return decompress0(in, out, -1);
 }

 public void close() {
  free(this.ctx);
 }

 private static native void initIDs();

 private static native long allocate();

 private static native void free(long ctx);

 private native int decompressBothHeap(
  byte[] in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type,
  int knownSize)
 throws DataFormatException;

 private native int decompressOnlyDestinationDirect(
  byte[] in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type,
  int knownSize)
 throws DataFormatException;

 private native int decompressOnlySourceDirect(
  ByteBuffer in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type,
  int knownSize)
 throws DataFormatException;

 private native int decompressBothDirect(
  ByteBuffer in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type,
  int knownSize)
 throws DataFormatException;
}
