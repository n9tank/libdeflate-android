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

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;

public class LibdeflateCompressor implements Closeable, AutoCloseable {
 static {
  Libdeflate.ensureAvailable();
 }
 public final long ctx;
 public final int lvl;
 public int mode;

 public LibdeflateCompressor(int level, int mode) {
  this.ctx = allocate(lvl = level);
  this.mode = mode;
 }

 public int compress(ByteBuffer in, ByteBuffer out) {
  int nativeType = mode;
  int result;
  int inAvail = in.remaining();
  long ctx=this.ctx;
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
  out.position(out.position() + result);
  in.position(in.position() + inAvail);
  return result;
 }

 public void close() {
  free(this.ctx);
 }

 public static native long allocate(int level);

 public static native void free(long ctx);

 public static native int compressBothHeap(
  long ctx, byte[] in, int inPos, int inSize, byte[] out, int outPos, int outSize, int type);

 public static native int compressOnlyDestinationDirect(
  long ctx,
  byte[] in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);

 public static native int compressOnlySourceDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type);

 public static native int compressBothDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);
}
