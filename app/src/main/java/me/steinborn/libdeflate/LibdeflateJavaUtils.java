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

public class LibdeflateJavaUtils {
 public final static int getBufSize(int size, int type) {
  int i=Math.max((size + 4999) / 5000 * 5, 5) + size;
  //b=5000 ((a+b-1)/b)*5+a;
  switch (type) {
   case 1:
    i += 6;
    break;
   case 2:
    i += 18;
    break;
  }
  return i;
 }
}
