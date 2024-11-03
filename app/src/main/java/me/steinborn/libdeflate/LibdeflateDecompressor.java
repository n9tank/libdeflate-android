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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;

public class LibdeflateDecompressor implements Closeable, AutoCloseable {
 static {
  Libdeflate.ensureAvailable();
 }
 public final long ctx;
 public final int mode;
 public LibdeflateDecompressor(int mode) {
  this.ctx = allocate();
  this.mode = mode;
 }
 public ByteBuffer decompressMalloc(ByteBuffer in, ByteBuffer out) throws Exception {
  while (true) {
   int ret=decompress(in, out);
   if (ret >= 0) {
    return out;
   } else {
    ByteBuffer old=out;
    out = ByteBuffer.allocateDirect(out.capacity() << 1);
    old.flip();
    out.put(old);
   }
  }
 }

 public int decompress(
  ByteBuffer in, ByteBuffer out)
 throws DataFormatException {
  int nativeType = mode;
  int inAvail = in.remaining();
  int outAvail = out.remaining();
  long ctx=this.ctx;
  long int64;
  if (in.isDirect()) {
   if (out.isDirect()) {
    int64 =
     decompressBothDirect(
     ctx,
     in,
     in.position(),
     inAvail,
     out,
     out.position(),
     outAvail,
     nativeType);
   } else {
    int64 =
     decompressOnlySourceDirect(
     ctx,
     in,
     in.position(),
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     outAvail,
     nativeType);
   }
  } else {
   int inPos = byteBufferArrayPosition(in);
   if (out.isDirect()) {
    int64 =
     decompressOnlyDestinationDirect(
     ctx,
     in.array(),
     inPos,
     inAvail,
     out,
     out.position(),
     outAvail,
     nativeType);
   } else {
    int64 =
     decompressBothHeap(
     ctx,
     in.array(),
     inPos,
     inAvail,
     out.array(),
     byteBufferArrayPosition(out),
     outAvail,
     nativeType);
   }
  }
  int outRealSize=(int)int64;
  out.position(out.position() + (outRealSize & 0x7fffffff));
  in.position(in.position() + (int)(int64 >>> 32));
  return outRealSize;
 }
 public void close() {
  free(this.ctx);
 }

 public static native long allocate();

 public static native void free(long ctx);

 public static native long decompressBothHeap(
  long ctx,
  byte[] in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type)
 throws DataFormatException;

 public static native long decompressOnlyDestinationDirect(
  long ctx,
  byte[] in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type)
 throws DataFormatException;

 public static native long decompressOnlySourceDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type)
 throws DataFormatException;

 public static native long decompressBothDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type)
 throws DataFormatException;
}
