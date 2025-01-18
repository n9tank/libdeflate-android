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

import java.nio.ByteBuffer;

public class LibdeflateCompressor implements AutoCloseable {
 static {
  Libdeflate.ensureAvailable();
 }
 public final long ctx;
 public final int lvl;
 public int mode;

 public LibdeflateCompressor(int level, int mode) {
  long ptr=allocate(lvl = level);
  if (ptr == 0)throw new OutOfMemoryError();
  this.ctx = ptr;
  this.mode = mode;
 }

 public int compress(ByteBuffer in, ByteBuffer out) {
  int nativeType = mode;
  int result;
  int inAvail=in.remaining();
  int outlen=out.remaining();
  long ctx=this.ctx;
  int inpos=in.position();
  int outpos=out.position();
  if (in.isDirect()) {
   if (out.isDirect()) {
    result =
     compressBothDirect(
     ctx, in, inpos, inAvail, out, outpos, outlen, nativeType);
   } else {
    result =
     compressOnlySourceDirect(
     ctx,
     in,
     inpos,
     inAvail,
     out.array(),
     out.arrayOffset() + outpos,
     outlen,
     nativeType);
   }
  } else {
   int inPos = in.arrayOffset() + inpos;
   if (out.isDirect()) {
    result =
     compressOnlyDestinationDirect(
     ctx, in.array(), inPos, inAvail, out, outpos, outlen, nativeType);
   } else {
    result =
     compressBothHeap(
     ctx,
     in.array(),
     inPos,
     inAvail,
     out.array(),
     out.arrayOffset() + outpos,
     outlen,
     nativeType);
   }
  }
  if (result < 0)throw new OutOfMemoryError();
  out.position(outpos + result);
  in.position(inpos + inAvail);
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
