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
 * Equivalent to {@link java.util.zip.Adler32}, but uses libdeflate's Adler-32 routines. As a
 * result, performance of this class is likely to be better than the JDK version.
 */
public class LibdeflateAdler32 implements Checksum {
 static {
  Libdeflate.ensureAvailable();
 }
 public int adler32 = 1;
 public void update(int b) {
  byte[] tmp = new byte[] {(byte) b};
  adler32 = adler32Heap(adler32, tmp, 0, 1);
 }
 public void update(byte[] b) {
  adler32 = adler32Heap(adler32, b, 0, b.length);
 }
 public void update(byte[] b, int off, int len) {
  adler32 = adler32Heap(adler32, b, off, len);
 }
 public void update(ByteBuffer buffer) {
  int pos = buffer.position();
  int limit = buffer.limit();
  int remaining = limit - pos;
  if (!buffer.isDirect())
   adler32 = adler32Heap(adler32, buffer.array(), buffer.arrayOffset() + buffer.position(), remaining);
  else 
   adler32 = adler32Direct(adler32, buffer, pos, remaining);
  buffer.position(limit);
 }
 public long getValue() {
  return ((long) adler32 & 0xffffffffL);
 }
 public void reset() {
  adler32 = 1;
 }
 public static native int adler32Heap(int adler32, byte[] array, int off, int len);
 public static native int adler32Direct(int adler32, ByteBuffer buf, int off, int len);
}
