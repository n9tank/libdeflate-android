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
import java.util.zip.Deflater;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;

/**
 * Represents a {@code libdeflate} compressor. This class contains compression methods for byte
 * arrays, NIO ByteBuffers, and the ability to compute a maximum possible bound for a given
 * compression format and byte count.
 *
 * <p><strong>Thread-safety</strong>: libdeflate compressors are not thread-safe, however using
 * multiple compressors per thread is permissible.
 */
public class LibdeflateCompressor implements Closeable, AutoCloseable {
 private static final int MINIMUM_COMPRESSION_LEVEL = 0;
 private static final int MAXIMUM_COMPRESSION_LEVEL = 12;
 private static final int DEFAULT_COMPRESSION_LEVEL = 6;

 static {
  Libdeflate.ensureAvailable();
 }

 final long ctx;

 /** Creates a new compressor with the default compression level. */
 public LibdeflateCompressor() {
  this(Deflater.DEFAULT_COMPRESSION);
 }

 /**
  * Creates a new compressor with the specified compression level.
  *
  * @param level the compression level to use, from 0 to 12
  * @throws IllegalArgumentException if the level is not within range
  */
 public LibdeflateCompressor(int level) {
  this.ctx = allocate(level);
 }

 /**
  * Compresses the entirety of {@code in} array into the {@code out} array.
  *
  * @param in the source array to compress
  * @param out the destination which will hold compressed data
  * @param type the compression container to use
  * @return a positive, non-zero integer with the size of the compressed output, or zero if the
  *     given output buffer was too small
  */
 public int compress(byte[] in, byte[] out, int type) {
  return (int) compressBothHeap(ctx, in, 0, in.length, out, 0, out.length, type);
 }

 /**
  * Compresses the given {@code in} array into the {@code out} array.
  *
  * @param in the source array to compress
  * @param inOff the offset into the source array
  * @param inLen the length into the source array from the offset
  * @param out the destination which will hold compressed data
  * @param outOff the offset into the source array
  * @param outLen the length into the source array from {@code outOff}
  * @param type the compression container to use
  * @return a positive, non-zero integer with the size of the compressed output, or zero if the
  *     given output buffer was too small
  * @throws IllegalArgumentException if the given offset and lengths are out of bounds or represent
  *     a negative range
  */
 public int compress(
  byte[] in, int inOff, int inLen, byte[] out, int outOff, int outLen, int type) {
  return (int) compressBothHeap(ctx, in, inOff, inLen, out, outOff, outLen, type);
 }

 /**
  * Compresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the
  * compression operation completes, the {@code position} of the output buffer will be incremented
  * by the number of bytes produced, and the input {@code position} will be incremented by the
  * number of bytes remaining.
  *
  * @param in the source byte buffer to compress
  * @param out the destination which will hold compressed data
  * @param type the compression container to use
  * @return a positive, non-zero integer with the size of the compressed output, or zero if the
  *     given output buffer was too small
  */
 public int compress(ByteBuffer in, ByteBuffer out, int type) {
  int nativeType = type;

  // Either ByteBuffer could be direct or heap.
  long result;
  int inAvail = in.remaining();
  if (in.isDirect()) {
   if (out.isDirect()) {
    result =
     compressBothDirect(
     ctx, in, in.position(), inAvail, out, out.position(), out.remaining(), nativeType);
   } else {
    result =
     compressOnlySourceDirect(
     ctx,
     in,
     in.position(),
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     out.remaining(),
     nativeType);
   }
  } else {
   int inPos = byteBufferArrayPosition(in);
   if (out.isDirect()) {
    result =
     compressOnlyDestinationDirect(
     ctx, in.array(), inPos, inAvail, out, out.position(), out.remaining(), nativeType);
   } else {
    result =
     compressBothHeap(
     ctx,
     in.array(),
     inPos,
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     out.remaining(),
     nativeType);
   }
  }

  out.position((int) (out.position() + result));
  in.position(in.position() + inAvail);
  return (int) result;
 }

 /** Closes the compressor. Any further operations on the compressor will fail. */
 @Override
 public void close() {
  free(this.ctx);
 }

 /**
  * Returns a worst-case upper case bound on the number of bytes of compressed data that may be
  * produced by compressing any buffer of length less than or equal to {@code count} using this
  * specified compressor and the specified format.
  *
  * <p>Note that this function is not necessary in many applications. With block-based compression,
  * it is usually preferable to separately store the uncompressed size of each block and to store
  * any blocks that did not compress to less than their original size uncompressed.
  *
  * @param count the maximum number of bytes to compute the upper bound for
  * @param type the compression type to use
  * @return the upper bound
  */

 /**
  * Returns a worst-case upper case bound on the number of bytes of compressed data that may be
  * produced by compressing any buffer of length less than or equal to {@code count} using any
  * compression option supported by libdeflate using the specified format.
  *
  * <p>Note that this function is not necessary in many applications. With block-based compression,
  * it is usually preferable to separately store the uncompressed size of each block and to store
  * any blocks that did not compress to less than their original size uncompressed.
  *
  * <p>This method is safe for use from multiple threads concurrently.
  *
  * @param count the maximum number of bytes to compute the upper bound for
  * @param type the compression type to use
  * @return the upper bound
  */

 /* Native function declarations. */
 private static native long allocate(int level);

 private static native void free(long ctx);

 static native long compressBothHeap(
  long ctx, byte[] in, int inPos, int inSize, byte[] out, int outPos, int outSize, int type);

 static native long compressOnlyDestinationDirect(
  long ctx,
  byte[] in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);

 static native long compressOnlySourceDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type);

 static native long compressBothDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);

}