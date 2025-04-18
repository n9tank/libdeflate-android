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
import java.util.zip.Checksum;

/**
 * Equivalent to {@link java.util.zip.CRC32}, but uses libdeflate's CRC-32 routines. As a result,
 * performance of this class is likely to be better than the JDK version.
 */
public class LibdeflateCRC32 implements Checksum {
 static {
  Libdeflate.ensureAvailable();
 }
 public int crc32 = 0;
 public void update(int b) {
  byte[] tmp = new byte[] {(byte) b};
  crc32 = crc32Heap(crc32, tmp, 0, 1);
 }
 public void update(byte[] b) {
  crc32 = crc32Heap(crc32, b, 0, b.length);
 }
 public void update(byte[] b, int off, int len) {
  crc32 = crc32Heap(crc32, b, off, len);
 }
 public void update(ByteBuffer buffer) {
  int pos = buffer.position();
  int limit = buffer.limit();
  int remaining = limit - pos;
  if (!buffer.isDirect()) 
   crc32 = crc32Heap(crc32, buffer.array(), buffer.arrayOffset() + buffer.position() , remaining);
  else
   crc32 = crc32Direct(crc32, buffer, pos, remaining);
  buffer.position(limit);
 }
 public long getValue() {
  return ((long) crc32 & 0xffffffffL);
 }
 public void reset() {
  crc32 = 0;
 }
 public static native int crc32Heap(int crc32, byte[] array, int off, int len);
 public static native int crc32Direct(int crc32, ByteBuffer buf, int off, int len);
}
