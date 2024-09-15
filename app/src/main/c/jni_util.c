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
#include "jni_util.h"

void throwException(JNIEnv *env, const char *type, const char *msg) {
  // We don't cache these, since they will only occur rarely.
  jclass klazz = (*env)->FindClass(env, type);

  if (klazz != 0) {
    (*env)->ThrowNew(env, klazz, msg);
  }
}
