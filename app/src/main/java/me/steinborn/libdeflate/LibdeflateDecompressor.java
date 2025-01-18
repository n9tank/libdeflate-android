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

import java.io.IOException;
import java.nio.ByteBuffer;

public class LibdeflateDecompressor implements AutoCloseable {
 static {
  Libdeflate.ensureAvailable();
 }
 public final long ctx;
 public int mode;
 public LibdeflateDecompressor(int mode) {
  long ptr=allocate();
  if (ptr == 0)throw new OutOfMemoryError();
  this.ctx = ptr;
  this.mode = mode;
 }
 public int decompress(ByteBuffer in, ByteBuffer out)throws IOException {
  int nativeType = mode;
  int inAvail = in.remaining();
  int outAvail = out.remaining();
  long ctx=this.ctx;
  long int64;
  int inpos=in.position();
  int outpos=out.position();
  if (in.isDirect()) {
   if (out.isDirect()) {
    int64 =
     decompressBothDirect(
     ctx,
     in,
     inpos,
     inAvail,
     out,
     outpos,
     outAvail,
     nativeType);
   } else {
    int64 =
     decompressOnlySourceDirect(
     ctx,
     in,
     inpos,
     inAvail,
     out.array(),
     out.arrayOffset() + outpos,
     outAvail,
     nativeType);
   }
  } else {
   int inPos=inpos + in.arrayOffset();
   if (out.isDirect()) {
    int64 =
     decompressOnlyDestinationDirect(
     ctx,
     in.array(),
     inPos,
     inAvail,
     out,
     outpos,
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
     out.arrayOffset() + outpos,
     outAvail,
     nativeType);
   }
  }
  //在jni抛错so文件会变大，况且只有正数范围的话-2
  if (int64 == -1l)throw new IOException();
  else if (int64 == -2l)throw new OutOfMemoryError();
  int outRealSize=(int)int64;
  out.position(outpos + (outRealSize & 0x7fffffff));
  in.position(inpos + (int)(int64 >>> 32));
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
  int type);

 public static native long decompressOnlyDestinationDirect(
  long ctx,
  byte[] in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);

 public static native long decompressOnlySourceDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  byte[] out,
  int outPos,
  int outSize,
  int type);

 public static native long decompressBothDirect(
  long ctx,
  ByteBuffer in,
  int inPos,
  int inSize,
  ByteBuffer out,
  int outPos,
  int outSize,
  int type);
}
